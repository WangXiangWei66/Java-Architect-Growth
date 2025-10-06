package com.ms.base;
import java.util.*;

//Transformer中的自注意力机制
//以翻译句子为例：
//        * 输入序列：`"The cat ate the fish"`
//        * 当模型处理 `"ate"` 时，注意力机制会让它：
//        * 高度关注 `"cat"`（主语）和 `"fish"`（宾语），
//        * 忽略无关词（如 `"the"`）
public class TF_AT {
    // 模拟 Token 的嵌入向量（实际中通过模型训练得到）
    // 训练后的嵌入向量和权重，强制 "ate" 关注 "cat" 和 "fish"
    static Map<String, float[]> tokenEmbeddings = new HashMap<>() {{
        put("The", new float[]{0.1f, 0.0f, 0.0f});   // 无关词
        put("cat", new float[]{2.0f, 0.0f, 0.0f});   // 主语（第一个维度突出）
        put("ate", new float[]{1.0f, 0.0f, 0.0f});   // Query 方向与 Key 对齐
        put("the", new float[]{0.1f, 0.0f, 0.0f});   // 无关词
        put("fish", new float[]{1.5f, 0.0f, 0.0f});  // 宾语
    }};

    // 模拟 Query/Key/Value 的权重矩阵（实际中是可学习的参数）
    // 简化权重矩阵，仅保留第一个维度
    //计算Query（用于"提问"）
    static float[][] W_Q = {{1.0f, 0.0f, 0.0f}, {0.0f, 0.0f, 0.0f}, {0.0f, 0.0f, 0.0f}};
    // 计算Key（用于"应答"）
    static float[][] W_K = {{1.0f, 0.0f, 0.0f}, {0.0f, 0.0f, 0.0f}, {0.0f, 0.0f, 0.0f}};
    // 计算Value（用于"携带信息"）
    static float[][] W_V = {{1.0f, 0.0f, 0.0f}, {0.0f, 0.0f, 0.0f}, {0.0f, 0.0f, 0.0f}};

    // 计算 Query/Key/Value（矩阵乘法简化版）
    static float[] matMul(float[] vector, float[][] matrix) {
        float[] result = new float[vector.length];
        for (int i = 0; i < matrix[0].length; i++) {
            for (int j = 0; j < vector.length; j++) {
                result[i] += vector[j] * matrix[j][i];
            }
        }
        return result;
    }

    // 生成所有 Token 的 Key 和 Value
    static Map<String, float[]> getKeysAndValues() {
        Map<String, float[]> kv = new HashMap<>();
        for (String token : tokenEmbeddings.keySet()) {
            float[] embedding = tokenEmbeddings.get(token);
            kv.put(token + "_K", matMul(embedding, W_K));
            kv.put(token + "_V", matMul(embedding, W_V));
        }
        return kv;
    }
    // 计算点积注意力分数（缩放后）
    static float dotProduct(float[] a, float[] b) {
        float sum = 0;
        for (int i = 0; i < a.length; i++) {
            sum += a[i] * b[i];
        }
        return sum / (float) Math.sqrt(a.length);
    }
    // 计算 "ate" 对其他 Token 的注意力权重
    static Map<String, Float> calculateAttention(String currentToken) {
        Map<String, float[]> kv = getKeysAndValues();
        float[] q = matMul(tokenEmbeddings.get(currentToken), W_Q);

        Map<String, Float> attentionScores = new LinkedHashMap<>();
        for (String token : tokenEmbeddings.keySet()) {
            if (token.equals(currentToken)) continue;
            float score = dotProduct(q, kv.get(token + "_K"));
            attentionScores.put(token, score);
        }

        // Softmax 归一化
        float sumExp = 0;
        for (float score : attentionScores.values()) {
            sumExp += Math.exp(score);
        }
        for (String token : attentionScores.keySet()) {
            attentionScores.put(token, (float) (Math.exp(attentionScores.get(token)) / sumExp));
        }
        return attentionScores;
    }

    public static void main(String[] args) {
        String currentToken = "ate";
        Map<String, Float> attentionWeights = calculateAttention(currentToken);

        System.out.println("当处理 \"" + currentToken + "\" 时，注意力权重：");
        for (Map.Entry<String, Float> entry : attentionWeights.entrySet()) {
            System.out.printf("  %-5s → %.2f\n", entry.getKey(), entry.getValue());
        }
    }
}