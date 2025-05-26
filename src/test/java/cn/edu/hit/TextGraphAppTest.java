package cn.edu.hit;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class TextGraphAppTest {

    static TextGraphApp.DirectedGraph graph;

    @BeforeAll
    static void setup() throws IOException {
        // 构建测试用图（请根据你项目的实际情况修改这部分）
        graph = TextGraphApp.buildGraphFromFile("src/main/java/cn/edu/hit/Easy Test.txt");
    }

    @Test
    void testShortestPath_case1_the_to_it() {
        String result = TextGraphApp.calcShortestPath(graph, "the", "it");
        System.out.println(result);
        assertEquals("Shortest path: the -> scientist -> analyzed -> it (length: 4)", result);
        System.out.println("Test Case 1 Passed.");
    }

    @Test
    void testShortestPath_case2_the_to_the() {
        String result = TextGraphApp.calcShortestPath(graph, "the", "the");
        System.out.println(result);
        assertEquals("Shortest path: the (length: 0)", result);
        System.out.println("Test Case 2 Passed.");
    }

    @Test
    void testShortestPath_case3_the_to_null() {
        String result = TextGraphApp.calcShortestPath(graph, "the", null);
        System.out.println(result);
        assertTrue(result.contains("the ->"));
        System.out.println("Test Case 3 Passed.");
    }

    @Test
    void testShortestPath_case4_good_to_it() {
        String result = TextGraphApp.calcShortestPath(graph, "good", "it");
        System.out.println(result);
        assertEquals("Start word is not in graph.", result);
        System.out.println("Test Case 4 Passed.");
    }

    @Test
    void testShortestPath_case5_the_to_good() {
        String result = TextGraphApp.calcShortestPath(graph, "the", "good");
        System.out.println(result);
        assertEquals("End word is not in graph.", result);
        System.out.println("Test Case 5 Passed.");
    }

    @Test
    void testShortestPath_case6_good_to_bad() {
        String result = TextGraphApp.calcShortestPath(graph, "good", "bad");
        System.out.println(result);
        assertEquals("Start word and end word are not in graph.", result);
        System.out.println("Test Case 6 Passed.");
    }

    @Test
    void testShortestPath_case7_again_to_the() {
        String result = TextGraphApp.calcShortestPath(graph, "again", "the");
        System.out.println(result);
        assertEquals("No path from again to the.", result);
        System.out.println("Test Case 7 Passed.");
    }

    @Test
    void testShortestPath_case8_good_to_null() {
        String result = TextGraphApp.calcShortestPath(graph, "good", null);
        System.out.println(result);
        assertEquals("Start word is not in graph and end word is null.", result);
        System.out.println("Test Case 8 Passed.");
    }
}
