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
import eu.stratosphere.hadoopcompatibility.example.ParquetFileWordCount;
import eu.stratosphere.test.testdata.WordCountData;
import eu.stratosphere.test.util.TestBase2;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.fs.Path;

import parquet.example.data.Group;
import parquet.example.data.simple.SimpleGroup;
import parquet.hadoop.ParquetWriter;
import parquet.hadoop.api.WriteSupport;
import parquet.hadoop.example.GroupWriteSupport;
import parquet.io.api.Binary;
import parquet.schema.PrimitiveType;
import parquet.schema.MessageType;
import parquet.schema.Type;


import java.io.File;
import java.io.IOException;


public class ParquetFileSourceTest extends TestBase2 {

    private String parquetFilePath;
    private String resultPath;
    private String counts;

    @Override
    protected void preSubmit() throws Exception {
        MessageType schema = generateTestGroupSchema();
        String[] testData = WordCountData.TEXT.split("\n");
        parquetFilePath = createTempParquetFile(schema, "parquet_file", testData);
        resultPath = getTempDirPath("result");
        counts = WordCountData.COUNTS;
    }

    /**
     * Returns the location of a file created by using the ParquetInputFormat.
     */
    private String createTempParquetFile(MessageType schema, String filename, String[] content) throws IOException {
        File f = createAndRegisterTempFile(filename);
        populateParquetFile(schema, f, content);
        return f.toURI().toString();
    }

    /**
     * Returns a Test Schema for Parquet named parquet_test with one text column (Binary)
     * named text_in_line.
     */
    private MessageType generateTestGroupSchema() {
        return new MessageType("parquet_test",
               new PrimitiveType(Type.Repetition.REPEATED,
               PrimitiveType.PrimitiveTypeName.BINARY,
               "text_in_line" ));
    }

    /**
     * In this method we populate a Parquet file using Groups.
     */
    private void populateParquetFile(MessageType schema, File f, String[] content) throws IOException{
        Path path = new Path(f.toURI());
        final MessageType finalSchema = schema;

        // init needs to be overridden as configuration cannot be passed in an other way.
        ParquetWriter<Group> parquetWriter = new ParquetWriter<Group>(path, new GroupWriteSupport() {
            @Override
            public WriteSupport.WriteContext init(Configuration configuration) {
                if (configuration.get(GroupWriteSupport.PARQUET_EXAMPLE_SCHEMA) == null) {
                    configuration.set(GroupWriteSupport.PARQUET_EXAMPLE_SCHEMA, finalSchema.toString());
                }
                return super.init(configuration);
            }
        });

        Group group = new SimpleGroup(schema);
        try {
            for(String line : content) {
                group.append("text_in_line", Binary.fromString(line));
            }
            parquetWriter.write(group);
        }
        finally {
            IOUtils.closeStream(parquetWriter);
        }
    }

    @Override
    protected Plan getTestJob() {
        ParquetFileWordCount wc = new ParquetFileWordCount();
        return wc.getPlan("1", parquetFilePath, resultPath);
    }


    @Override
    protected void postSubmit() throws Exception {
        compareResultsByLinesInMemory(counts, resultPath);
    }

}