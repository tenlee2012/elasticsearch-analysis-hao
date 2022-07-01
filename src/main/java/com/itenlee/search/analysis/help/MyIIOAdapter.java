package com.itenlee.search.analysis.help;

import com.hankcs.hanlp.corpus.io.IIOAdapter;
import org.elasticsearch.core.PathUtils;
import org.elasticsearch.plugin.analysis.hao.AnalysisHaoPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author tenlee
 * @date 2020/9/24
 */
public class MyIIOAdapter implements IIOAdapter {
    /**
     * 打开一个文件以供读取
     *
     * @param path 文件路径
     * @return 一个输入流
     * @throws IOException 任何可能的IO异常
     */
    @Override
    public InputStream open(String path) throws IOException {
        return new FileInputStream(
                PathUtils.get(new File(AnalysisHaoPlugin.class.getProtectionDomain().getCodeSource().getLocation().getPath())
                        .getParent()).toAbsolutePath().resolve(path).toFile()
        );
    }

    /**
     * 创建一个新文件以供输出
     *
     * @param path 文件路径
     * @return 一个输出流
     * @throws IOException 任何可能的IO异常
     */
    @Override
    public OutputStream create(String path) throws IOException {
        return new FileOutputStream(
                PathUtils.get(new File(AnalysisHaoPlugin.class.getProtectionDomain().getCodeSource().getLocation().getPath())
                        .getParent()).toAbsolutePath().resolve(path).toFile()
        );
    }
}
