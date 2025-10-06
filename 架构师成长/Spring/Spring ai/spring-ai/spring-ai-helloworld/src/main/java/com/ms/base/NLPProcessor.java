package com.ms.base;

import java.util.*;

public class NLPProcessor {
    // 模拟词汇表（Token → ID）
    //DeepSeek实际包含30万+ Token（中英混合+特殊符号）
    private static final Map<String, Integer> VOCAB = new HashMap<>() {{
        put("[PAD]", 0);   // 填充符
        put("[UNK]", 1);   // 未知Token
        put("你", 3);
        put("好", 4);
        put("世界", 5);
        put("！", 6);
        put("人工", 7);
        put("智能", 8);
    }};

    // 模拟预训练的Embedding矩阵（ID → 向量）  3维
    // DeepSeek的维度通常为4096/8192
    // {0.12f, -0.05f, 0.23f, ..., 0.01f},  "你"的4096维向量（省略4090+个值）
    private static final float[][] EMBEDDINGS = {
            {0.0f, 0.0f, 0.0f},  // ID=0 ([PAD])
            {0.1f, 0.1f, 0.1f},   // ID=1 ([UNK])
            {0.0f, 0.0f, 0.0f},   // ID=2 (未使用)
            {0.2f, -0.5f, 0.7f},  // ID=3 ("你")
            {-0.3f, 0.6f, 0.1f},  // ID=4 ("好")
            {0.4f, 0.8f, -0.2f},  // ID=5 ("世界")
            {0.5f, -0.1f, 0.3f},  // ID=6 ("！")
            {0.5f, -0.1f, 0.3f},  // ID=7 ("人工")
            {0.1f, 0.4f, -0.5f}  // ID=8 ("智能")
    };

    // 简单分词逻辑（混合按词和按字）
    public static List<Integer> tokenize(String text) {
        List<Integer> tokenIds = new ArrayList<>();

        // 优先尝试匹配多字词
        int i = 0;
        while (i < text.length()) {
            boolean found = false;
            // 检查最长匹配（从最长词开始尝试）
            for (int len = 3; len >= 1; len--) {
                if (i + len <= text.length()) {
                    String word = text.substring(i, i + len);
                    if (VOCAB.containsKey(word)) {
                        tokenIds.add(VOCAB.get(word));
                        i += len;
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                // 未匹配任何词，使用UNK
                tokenIds.add(VOCAB.get("[UNK]"));
                i++;
            }
        }
        return tokenIds;
    }

    // 单个ID转向量
    public static float[] idToEmbedding(int tokenId) {
        if (tokenId < 0 || tokenId >= EMBEDDINGS.length) {
            return EMBEDDINGS[VOCAB.get("[UNK]")]; // 越界返回UNK向量
        }
        return EMBEDDINGS[tokenId];
    }

    // 批量转换Token IDs为向量
    public static float[][] tokensToEmbeddings(List<Integer> tokenIds) {
        float[][] result = new float[tokenIds.size()][];
        for (int i = 0; i < tokenIds.size(); i++) {
            result[i] = idToEmbedding(tokenIds.get(i));
        }
        return result;
    }
    // 流程测试:文本 → Token ID → 向量（Embedding）
    public static void main(String[] args) {
        String text = "你好世界！人工智能";

        // 1. 文本 → Token IDs
        List<Integer> tokenIds = tokenize(text);
        System.out.println("原始文本: " + text);
        System.out.println("Token IDs: " + tokenIds);

        // 2. Token IDs → 向量序列
        float[][] embeddings = tokensToEmbeddings(tokenIds);

        // 打印结果
        System.out.println("\n向量表示:");
        for (int i = 0; i < tokenIds.size(); i++) {
            System.out.printf("Token ID %3d → %s\n",
                    tokenIds.get(i),
                    Arrays.toString(embeddings[i]));
        }
    }
}