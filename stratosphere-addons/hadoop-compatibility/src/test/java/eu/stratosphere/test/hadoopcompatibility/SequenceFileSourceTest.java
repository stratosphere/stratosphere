package eu.stratosphere.test.hadoopcompatibility;
/***********************************************************************************************************************
 * Copyright (C) 2010-2013 by the Stratosphere project (http://stratosphere.eu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 **********************************************************************************************************************/

import eu.stratosphere.api.common.Plan;
import eu.stratosphere.hadoopcompatibility.example.SequenceFileWordCount;
import eu.stratosphere.test.testdata.WordCountData;
import eu.stratosphere.test.util.TestBase2;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;

public class SequenceFileSourceTest extends TestBase2 {

    private String sequenceFilePath;
    private String resultPath;
    private String counts;

    protected String createTempSeqFile(String filename, String[] content) throws IOException {
        File f = createAndRegisterTempFile(filename);
        populateSeqFile(f, content);
        return f.toURI().toString();

    }

    private void populateSeqFile(File f, String[] content) throws  IOException{
        URI uri = f.toURI();
        Configuration conf = new JobConf();
        FileSystem fs = FileSystem.get(uri, conf);
        Path path = new Path(uri);
        IntWritable key = new IntWritable();
        Text value = new Text();
        SequenceFile.Writer writer = null;
        try {
            writer = SequenceFile.createWriter(fs, conf, path,
                    key.getClass(), value.getClass());
            for (int i = 0; i < content.length; i++) {
                key.set(i);
                value.set(content[i]);
                writer.append(key, value);
            }
        } finally {
            IOUtils.closeStream(writer);
        }
    }

    @Override
    protected void preSubmit() throws Exception {
        String[] testData = WordCountData.TEXT.split("\n");
        sequenceFilePath = createTempSeqFile("seq_file", testData);
        resultPath = getTempDirPath("result");
        counts = WordCountData.COUNTS;
    }

    @Override
    protected Plan getTestJob() {
        SequenceFileWordCount wc = new SequenceFileWordCount();
        return wc.getPlan("1", sequenceFilePath, resultPath);
    }

    @Override
    protected void postSubmit() throws Exception {
        compareResultsByLinesInMemory(counts, resultPath);
    }

}
