package cn.edu.hit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.print.DocFlavor;
import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

//白盒测试用例
class QueryBridgeWordsTest {

    // 创建一个简单的图结构供测试使用
    static TextGraphApp.DirectedGraph graph;

    @BeforeAll
    static void setup() throws IOException {
        // 构建测试用图
        graph = TextGraphApp.buildGraphFromFile("src/main/java/cn/edu/hit/Easy Test.txt");
    }

    @Test
    void testBothWordsNotInGraph() {
        String result = TextGraphApp.queryBridgeWords(graph, "good", "bad");
        System.out.println(result);
        assertEquals("No good and bad in the graph!", result);
        System.out.println("Test Case 1 Passed.");
    }

    @Test
    void testWord1NotInGraph() {
        String result = TextGraphApp.queryBridgeWords(graph, "the", "good");
        System.out.println(result);
        assertEquals("No good in the graph!", result);
        System.out.println("Test Case 2 Passed.");
    }

    @Test
    void testWord2NotInGraph() {
        String result = TextGraphApp.queryBridgeWords(graph, "good", "the");
        System.out.println(result);
        assertEquals("No good in the graph!", result);
        System.out.println("Test Case 3 Passed.");
    }

    @Test
    void testNoMidWords() {
        String result = TextGraphApp.queryBridgeWords(graph, "again", "the");
        System.out.println(result);
        assertEquals("No bridge words from again to the!", result);
        System.out.println("Test Case 4 Passed.");
    }

    @Test
    void testNoBridgeWords() {
        String result = TextGraphApp.queryBridgeWords(graph, "it", "the");
        System.out.println(result);
        assertEquals("No bridge words from it to the!", result);
        System.out.println("Test Case 5 Passed.");
    }
    @Test
    void testHasBridgeWords() {
        String result = TextGraphApp.queryBridgeWords(graph, "the", "so");
        System.out.println(result);
        assertTrue(result.contains("The bridge words from the to so is/are:"));
        assertTrue(result.contains("data"));
        System.out.println("Test Case 6 Passed.");
    }
}
