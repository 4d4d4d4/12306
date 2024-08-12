package com.train.common.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.output.ByteArrayOutputStream;

public class CompressUtil {
    // 压缩
	public static String compress(String str){
        // 创建一个Deflater实例，设置压缩级别为Deflater.BEST_COMPRESSION，这将尽可能地减小输出大小，但可能牺牲压缩速度。
        Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
        // 将输入字符串转换为字节数组，使用UTF-8编码。
        // 设置Deflater的输入为上述字节数组。
        deflater.setInput(str.getBytes(StandardCharsets.UTF_8));
        deflater.finish();
        final byte[] bytes = new byte[8192];
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(8192);
        // 使用一个循环，通过调用deflate()方法将压缩后的数据写入到ByteArrayOutputStream中，直到所有数据都被压缩完毕
        while (!deflater.finished()) {
            int length = deflater.deflate(bytes);
            outputStream.write(bytes, 0, length);
        }
        deflater.end();
        String s =  Base64.encodeBase64String(outputStream.toByteArray());
        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return s;
    }

    // 解压
    public static String uncompress(String str){
        // 解码Base64编码的字符串回原始字节数组。
        byte[] decode = Base64.decodeBase64(str);
        // 创建一个Inflater实例并设置其输入为解码后的字节数组。
        Inflater inflater = new Inflater();
        inflater.setInput(decode);
        final byte[] bytes = new byte[8192];
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(8192);
        try {
            // 使用循环读取解压缩的数据，写入到ByteArrayOutputStream中，直到所有数据都已解压缩。
            while (!inflater.finished()) {
                int length = inflater.inflate(bytes);
                outputStream.write(bytes, 0, length);
            }
        } catch (DataFormatException e) {
            e.printStackTrace();
        } finally {
            inflater.end();
        }
        try {
            // 将ByteArrayOutputStream的内容转换回字符串，使用UTF-8编码。
            String s =  outputStream.toString("UTF-8");
            outputStream.close();
            return s;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
