package com.train.common.utils;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentSampleModel;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Base64;

import javax.imageio.ImageIO;

import com.alibaba.fastjson.JSONObject;

import com.train.common.utils.CompressUtil;
/**
 * <dl>
 * <dt><b>类功能概述</b></dt><dd>
 * 本类用于 : 
 * </dd></dl>
 * <dl><dt><b>版本历史</b></dt><dd>
 * <ul>
 * <li>Version : 1.00</li>
 * <li>Date    : 2019-10-23 | 10:52:51 AM</li>
 * <li>Author  : yaojunWang.</li>
 * <li>History : 新建类.</li>
 * </ul>
 * </dd></dl>
 * @Copyright Copyright © 2019, QIANCHI. All rights reserved.
 * @Author yaojunWang.
 */
public class ImageUtil {
    
    public static String getImageBgrBase64(BufferedImage image) {
        byte[] data = getBgr(image);
        JSONObject bgrImage = new JSONObject();
        bgrImage.put("image", Base64.getEncoder().encodeToString(data));
        bgrImage.put("width", image.getWidth());
        bgrImage.put("height", image.getHeight());
        return CompressUtil.compress(bgrImage.toJSONString());
    }
    
    public static ByteArrayInputStream imageCompress(InputStream srcFile,int width, int height) {
        try {
            Image image = ImageIO.read(srcFile);
            
            BufferedImage newImage = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_RGB); 
            Graphics2D g = newImage.createGraphics();
            newImage = g.getDeviceConfiguration().createCompatibleImage(width, height, Transparency.TRANSLUCENT);
            g.dispose();
            g = newImage.createGraphics();
            Image scaled = image.getScaledInstance(width, height, BufferedImage.SCALE_AREA_AVERAGING);
            g.drawImage(scaled, 0, 0, null);
            g.dispose();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(newImage, "png", out);
            return new ByteArrayInputStream(out.toByteArray());
            
        } catch (FileNotFoundException fnf) {
            fnf.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    public static ByteArrayInputStream imageCompress(BufferedImage srcFile,int width, int height) {
        try {
            BufferedImage newImage = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_RGB); 
            Graphics2D g = newImage.createGraphics();
            newImage = g.getDeviceConfiguration().createCompatibleImage(width, height, Transparency.TRANSLUCENT);
            g.dispose();
            g = newImage.createGraphics();
            Image scaled = srcFile.getScaledInstance(width, height, BufferedImage.SCALE_AREA_AVERAGING);
            g.drawImage(scaled, 0, 0, null);
            g.dispose();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(newImage, "png", out);
            return new ByteArrayInputStream(out.toByteArray());
            
        } catch (FileNotFoundException fnf) {
            fnf.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    public static BufferedImage bufferedImageCompress(BufferedImage srcFile,int width, int height) {
        try {
            BufferedImage newImage = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_RGB); 
            Graphics2D g = newImage.createGraphics();
            newImage = g.getDeviceConfiguration().createCompatibleImage(width, height, Transparency.TRANSLUCENT);
            g.dispose();
            g = newImage.createGraphics();
            Image scaled = srcFile.getScaledInstance(width, height, BufferedImage.SCALE_AREA_AVERAGING);
            g.drawImage(scaled, 0, 0, null);
            g.dispose();
            return newImage;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    private static final int[] BGR_TYPE = { 0, 1, 2 };
    private static byte[] getBgr(BufferedImage image) {
        byte[] matrixBGR;
        if (isBgr(image)) {
            matrixBGR = (byte[]) image.getData().getDataElements(0, 0, image.getWidth(), image.getHeight(), null);
        } else {
            // ARGB格式图像数据
            int intrgb[] = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
            matrixBGR = new byte[image.getWidth() * image.getHeight() * 3];
            int len = intrgb.length;
            // ARGB转BGR格式
            for (int i = 0; i < len; i++) {
                matrixBGR[i * 3] = (byte) (intrgb[i] & 0xff);
                matrixBGR[i * 3 + 1] = (byte) ((intrgb[i] >> 8) & 0xff);
                matrixBGR[i * 3 + 2] = (byte) ((intrgb[i] >> 16) & 0xff);
            }
        }
        return matrixBGR;
    }
    private static boolean isBgr(BufferedImage image) {
        if (image.getType() == BufferedImage.TYPE_3BYTE_BGR && image.getData().getSampleModel() instanceof ComponentSampleModel) {
            ComponentSampleModel sampleModel = (ComponentSampleModel) image.getData().getSampleModel();
            return Arrays.equals(sampleModel.getBandOffsets(), BGR_TYPE);
        }
        return false;
    }
}

