//package cn.edu.hit;
//
//import guru.nidi.graphviz.attribute.Label;
//import guru.nidi.graphviz.engine.*;
//import guru.nidi.graphviz.model.*;
//
//import java.io.File;
//import java.io.IOException;
//import static guru.nidi.graphviz.model.Factory.*;
//
//public class GraphTest {
//    public static void main(String[] args) throws IOException {
//        MutableNode a = mutNode("A");
//        MutableNode b = mutNode("B");
//        a.addLink(to(b).with(Label.of("edge")));
//
//        MutableGraph g = mutGraph("example").setDirected(true).add(a, b);
//        Graphviz.fromGraph(g).render(Format.PNG).toFile(new File("simple.png"));
//        System.out.println("Graph saved to simple.png");
//    }
//}
