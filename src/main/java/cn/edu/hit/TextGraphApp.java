package cn.edu.hit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.engine.GraphvizCmdLineEngine;


import static guru.nidi.graphviz.model.Factory.mutGraph;
import static guru.nidi.graphviz.model.Factory.mutNode;
import static guru.nidi.graphviz.model.Factory.to;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import org.apache.commons.io.FilenameUtils;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;


public class TextGraphApp {
    /**
     * 日志输出
     */
    private static final Logger LOGGER = Logger.getLogger(TextGraphApp.class.getName());
    /**
     * 安全RANDOM
     */
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();


    //图节点
    public static class Node {

        /**
         * 当前节点所表示的单词。
         */
        String word;

        /**
         * 当前节点的所有有向边，键为目标节点，值为该边出现的次数
         */
        Map<Node, Integer> edges = new HashMap<>();
        //创建新节点对象
        Node(String word) {
            this.word = word;
        }

        @Override
        public String toString() {
            return word;
        }
    }
    //构建有向图
    public static class DirectedGraph {
        /**
         * 所有单词与其对应节点的映射关系，用于快速查找图中的节点。
         */
        Map<String, Node> nodes = new HashMap<>();

        //获取节点（不分大小写）
        Node getNode(String word) {
            return nodes.get(word.toLowerCase());
        }

        //添加边
        void addEdge(String from, String to) {
            from = from.toLowerCase();
            to = to.toLowerCase();
            Node fromNode = nodes.computeIfAbsent(from, Node::new);
            Node toNode = nodes.computeIfAbsent(to, Node::new);
            fromNode.edges.put(toNode, fromNode.edges.getOrDefault(toNode, 0) + 1);
        }

        //展示有向图
        void showDirectedGraph() {
            StringBuilder sb = new StringBuilder();
            //对每个Node进行遍历
            for (Node node : nodes.values()) {
                //遍历当前节点的所有边
                for (Map.Entry<Node, Integer> entry : node.edges.entrySet()) {
                    String line = node.word + " -> " + entry.getKey().word + " (weight: " + entry.getValue() + ")";
                    //LOGGER.info(line);
                    System.out.println(line);

                    sb.append(line).append("\n");
                }
            }
            try {
                Files.write(Paths.get("graph_show.txt"), sb.toString().getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                System.err.println("Failed to write graph to file.");
            }
        }

        //导出图像（借用外部工具Graphviz）
        void exportGraphToImage(String outputFilePath) {
            MutableGraph g = mutGraph("Graph").setDirected(true);
            Map<String, MutableNode> gvNodes = new HashMap<>();

            for (Node node : nodes.values()) {
                gvNodes.putIfAbsent(node.word, mutNode(node.word)); //创建源节点
                for (Map.Entry<Node, Integer> edge : node.edges.entrySet()) {
                    gvNodes.putIfAbsent(edge.getKey().word, mutNode(edge.getKey().word));
                    gvNodes.get(node.word).addLink(to(gvNodes.get(edge.getKey().word))
                            .with(Label.of(String.valueOf(edge.getValue()))));
                }
            }

            //将所有节点添加到图中
            for (MutableNode n : gvNodes.values()) {
                g.add(n);
            }

            //导出图片文件
            try {
                Graphviz.fromGraph(g).render(Format.PNG).toFile(new File(outputFilePath));
                //LOGGER.info("Graph image exported to: " + outputFilePath);
                System.out.println("Graph image exported to: " + outputFilePath);
            } catch (IOException e) {
                System.err.println("Failed to export graph image: " + e.getMessage());
            }
        }
    }

    //辅助函数：清洗文本（只保留英文小写单词，返回一个单词列表）
    static List<String> cleanWords(String text) {
        return Arrays.stream(text.toLowerCase()
                        .replaceAll("[^a-z\\s]", " ")
                        .split("\\s+"))
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList());
    }

    //读取文本文件内容，提取单词构建有向图
    static DirectedGraph buildGraphFromFile(String filename) throws IOException {
// 确保文件名不包含非法路径元素（防止路径遍历）
        String safeName = FilenameUtils.getName(filename);
        if (!filename.endsWith(safeName)) {
            throw new SecurityException("Illegal file path detected: " + filename);
        }

// 安全验证通过，继续使用原始路径读取文件
        String content = Files.readString(Paths.get(filename));


        List<String> words = cleanWords(content);
        DirectedGraph graph = new DirectedGraph();
        //遍历所有相邻单词对
        for (int i = 0; i < words.size() - 1; i++) {
            graph.addEdge(words.get(i), words.get(i + 1));
        }
        return graph;
    }

    //查询桥接词
    static String queryBridgeWords(DirectedGraph graph, String word1, String word2) {
        Node n1 = graph.getNode(word1);
        Node n2 = graph.getNode(word2);
        if (n1 == null && n2 == null) {
            return "No " + word1 + " and " + word2 + " in the graph!";
        }
        if (n1 == null) {
            return "No " + word1 + " in the graph!";
        }
        if (n2 == null) {
            return "No " + word2 + " in the graph!";
        }
        List<String> bridges = new ArrayList<>();
        for (Node mid : n1.edges.keySet()) {
            if (mid.edges.containsKey(n2)) {
                bridges.add(mid.word);
            }
        }
        if (bridges.isEmpty()) {
            return "No bridge words from " + word1 + " to " + word2 + "!";
        }
        return "The bridge words from " + word1 + " to " + word2 + " is/are: " + String.join(", ", bridges) + ".";
    }

    //根据输入文本和桥接词生成扩展文本
    static String generateNewText(DirectedGraph graph, String inputText) {
        List<String> words = cleanWords(inputText);
        List<String> result = new ArrayList<>();
        for (int i = 0; i < words.size() - 1; i++) {
            result.add(words.get(i));
            Node from = graph.getNode(words.get(i));
            Node to = graph.getNode(words.get(i + 1));
            if (from != null && to != null) {
                List<String> bridges = new ArrayList<>();
                for (Node mid : from.edges.keySet()) {
                    if (mid.edges.containsKey(to)) {
                        bridges.add(mid.word);
                    }
                }
                if (!bridges.isEmpty()) {
                    result.add(bridges.get(SECURE_RANDOM.nextInt(bridges.size())));
                }
            }
        }
        //补上最后一个单词，并合并文本
        result.add(words.get(words.size() - 1));
        return String.join(" ", result);
    }

    //查询两词间最短路径
    static String calcShortestPath(DirectedGraph graph, String start, String end) {
        Node source = null;
        Node target = null;
        if (start == null) {
            return "Start word is NULL";
        } else {
            source = graph.getNode(start);
        }

        if (end != null) {
            target = graph.getNode(end);
        }


        if (source == null) {
            if (end == null) {
                return "Start word is not in graph and end word is null.";
            } else if (target == null) {
                return "Start word and end word are not in graph.";
            } else {
                    return "Start word is not in graph.";
            }
        }

        // 单词到所有节点的最短路径
        if (end == null || end.isEmpty()) {
            Map<Node, Integer> dist = new HashMap<>();
            Map<Node, Node> prev = new HashMap<>();
            PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparingInt(dist::get));
            for (Node node : graph.nodes.values()) {
                dist.put(node, Integer.MAX_VALUE);
            }
            dist.put(source, 0);
            queue.add(source);

            while (!queue.isEmpty()) {
                Node u = queue.poll();
                for (Map.Entry<Node, Integer> entry : u.edges.entrySet()) {
                    Node v = entry.getKey();
                    int alt = dist.get(u) + entry.getValue();
                    if (alt < dist.get(v)) {
                        dist.put(v, alt);
                        prev.put(v, u);
                        queue.remove(v);
                        queue.add(v);
                    }
                }
            }

            StringBuilder result = new StringBuilder("Shortest paths from \"" + start + "\":\n");
            for (Node node : graph.nodes.values()) {
                if (node == source) {
                    continue;
                }
                if (dist.get(node) == Integer.MAX_VALUE) {
                    result.append("No path to ").append(node.word).append(".\n");
                } else {
                    List<String> path = new ArrayList<>();
                    for (Node at = node; at != null; at = prev.get(at)) {
                        path.add(at.word);
                    }
                    Collections.reverse(path);
                    result.append("To ").append(node.word)
                            .append(": ").append(String.join(" -> ", path))
                            .append(" (length: ").append(dist.get(node)).append(")\n");
                }
            }
            return result.toString();
        }

        // 起点和终点都存在时，执行原有路径计算逻辑
        if (target == null) {
            return "End word is not in graph.";
        }

        Map<Node, Integer> dist = new HashMap<>();
        Map<Node, Node> prev = new HashMap<>();
        PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparingInt(dist::get));
        for (Node node : graph.nodes.values()) {
            dist.put(node, Integer.MAX_VALUE);
        }
        dist.put(source, 0);
        queue.add(source);

        while (!queue.isEmpty()) {
            Node u = queue.poll();
            for (Map.Entry<Node, Integer> entry : u.edges.entrySet()) {
                Node v = entry.getKey();
                int alt = dist.get(u) + entry.getValue();
                if (alt < dist.get(v)) {
                    dist.put(v, alt);
                    prev.put(v, u);
                    queue.remove(v);
                    queue.add(v);
                }
            }
        }

        if (!dist.containsKey(target) || dist.get(target) == Integer.MAX_VALUE) {
            return "No path from " + start + " to " + end + ".";
        }

        List<String> path = new ArrayList<>();
        for (Node at = target; at != null; at = prev.get(at)) {
            path.add(at.word);
        }
        Collections.reverse(path);
        return "Shortest path: " + String.join(" -> ", path) + " (length: " + dist.get(target) + ")";
    }


    //计算PageRank
    static Map<String, Double> calcPageRank(DirectedGraph graph, double d, double epsilon) {
        Map<String, Double> pr = new HashMap<>();
        int nodeCount = graph.nodes.size();
        //int N = graph.nodes.size();
        for (String node : graph.nodes.keySet()) {
            pr.put(node, 1.0 / nodeCount);
        }

        boolean converged = false;
        while (!converged) {
            Map<String, Double> newPr = new HashMap<>();
            double danglingPR = 0;

            // 收集所有出度为0节点的总贡献
            for (String node : graph.nodes.keySet()) {
                if (graph.getNode(node).edges.isEmpty()) {
                    danglingPR += pr.get(node);
                }
            }
            double distributedDanglingPR = danglingPR / nodeCount;

            double maxDiff = 0;  // 最大变化量
            for (String u : graph.nodes.keySet()) {
                double sum = 0;
                for (Node v : graph.nodes.values()) {
                    if (v.edges.containsKey(graph.getNode(u))) {
                        sum += pr.get(v.word) / v.edges.size();
                    }
                }
                double newVal = (1 - d) / nodeCount + d * (sum + distributedDanglingPR);
                newPr.put(u, newVal);
                maxDiff = Math.max(maxDiff, Math.abs(newVal - pr.get(u)));
            }

            pr = newPr;
            converged = maxDiff < epsilon;
        }
        return pr;
    }


    //在图上随机游走
    static List<String> randomWalk(DirectedGraph graph, boolean stepByStep) {
        List<String> walk = new ArrayList<>();
        List<Node> nodeList = new ArrayList<>(graph.nodes.values());
        Node current = nodeList.get(SECURE_RANDOM.nextInt(nodeList.size())); //随机选择起点
        Set<String> visitedEdges = new HashSet<>();

        walk.add(current.word);
        Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8.name());


        while (!current.edges.isEmpty()) {
            List<Node> neighbors = new ArrayList<>(current.edges.keySet());
            Node next = neighbors.get(SECURE_RANDOM.nextInt(neighbors.size()));  //每次随机选择一个邻居继续走
            String edgeKey = current.word + "->" + next.word;
            if (visitedEdges.contains(edgeKey)) {
                break;
            }
            visitedEdges.add(edgeKey);

            current = next;
            walk.add(current.word);

            // 如果是 step-by-step 模式，每次询问
            if (stepByStep) {
                System.out.println("Current walk: " + String.join(" -> ", walk));
                System.out.print("Continue? (y/n): ");
                String input = scanner.nextLine().trim().toLowerCase();
                if (!input.equals("y")) {
                    break;
                }
            }
        }

        // 最终 walk 打印
        System.out.println("Random walk finished:");
        System.out.println(String.join(" -> ", walk));

        // 写入文件
        try {
            Files.write(Paths.get("random_walk.txt"), String.join(" ", walk).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.err.println("Failed to write walk to file.");
        }

        return walk;
    }



    //主程序（功能询问+结果展示）
    public static void main(String[] args) throws IOException {
        Graphviz.useEngine(new GraphvizCmdLineEngine()); // 调用外部库绘图
        Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8.name());
        System.out.print("Enter the file path: ");
        String path = scanner.nextLine();
        DirectedGraph graph = buildGraphFromFile(path);
        System.out.print("是否绘制有向图？(y/n): ");
        String drawChoice = scanner.nextLine().trim().toLowerCase();
        if (drawChoice.equals("y") || drawChoice.equals("yes")) {
            graph.exportGraphToImage("graph.png");
            System.out.println("图已绘制并保存为 graph.png");
            System.out.println("Graph built!");
        } else {
            System.out.println("跳过绘制图像。");
        }
        while (true) {
            System.out.println("\n选择功能:");
            System.out.println("1. 展示有向图");
            System.out.println("2. 查询桥接词");
            System.out.println("3. 根据桥接词生成新文本");
            System.out.println("4. 两个单词间最短路径");
            System.out.println("5. 计算PageRank");
            System.out.println("6. 随机游走");
            System.out.println("7. 退出");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    graph.showDirectedGraph();
                    break;
                case "2":
                    System.out.print("Enter word1: ");
                    String w1 = scanner.nextLine();
                    System.out.print("Enter word2: ");
                    String w2 = scanner.nextLine();
                    String bridgeResult = queryBridgeWords(graph, w1, w2);
                    System.out.println(bridgeResult);
                    try {
                        Files.write(Paths.get("bridge_words.txt"), bridgeResult.getBytes(StandardCharsets.UTF_8));
                    } catch (IOException e) {
                        System.err.println("Failed to write bridge words to file.");
                    }
                    break;
                case "3":
                    System.out.print("Enter new text: ");
                    String newText = scanner.nextLine();
                    System.out.println(generateNewText(graph, newText));
                    String generated = generateNewText(graph, newText);
                    try {
                        Files.write(Paths.get("new_text.txt"), generated.getBytes(StandardCharsets.UTF_8.name()));
                    } catch (IOException e) {
                        System.err.println("Failed to write new text to file.");
                    }
                    break;
                case "4":
                    System.out.print("Enter start word: ");
                    String start = scanner.nextLine().trim();  // 加 trim() 去掉多余空格
                    System.out.print("Enter end word (leave empty to find paths to all nodes): ");
                    String end = scanner.nextLine().trim();

                    String pathResult = calcShortestPath(graph, start, end);
                    System.out.println(pathResult);

                    try {
                        Files.write(Paths.get("shortest_path.txt"), pathResult.getBytes(StandardCharsets.UTF_8.name()));
                        System.out.println("Result written to shortest_path.txt.");
                    } catch (IOException e) {
                        System.err.println("Failed to write shortest path to file.");
                    }
                    break;
                case "5":
                    Map<String, Double> pr = calcPageRank(graph, 0.85, 1e-6);
                    StringBuilder prContent = new StringBuilder();
                    for (Map.Entry<String, Double> entry : pr.entrySet()) {
                        String line = String.format("%s: %.6f", entry.getKey(), entry.getValue());
                        System.out.println(line);
                        prContent.append(line).append("\n");
                    }
                    try {
                        Files.write(Paths.get("pagerank.txt"), prContent.toString().getBytes(StandardCharsets.UTF_8.name()));
                    } catch (IOException e) {
                        System.err.println("Failed to write pagerank to file.");
                    }
                    break;
                case "6":
                    System.out.println("Random walk mode:");
                    System.out.println("1. Generate full walk automatically");
                    System.out.println("2. Step by step (ask after each step)");
                    String mode = scanner.nextLine();

                    boolean stepByStep = mode.equals("2");
                    List<String> walk = randomWalk(graph, stepByStep);
                    break;
                case "7":
                    System.out.println("Exiting...");
                    return;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }
}
