package eu.stratosphere.hadoopcompatibility.datatypes.parquet;
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

import eu.stratosphere.hadoopcompatibility.datatypes.HadoopTypeConverter;
import eu.stratosphere.types.NullValue;
import eu.stratosphere.types.Record;
import eu.stratosphere.types.StringValue;
import eu.stratosphere.types.Value;


import parquet.example.data.simple.SimpleGroup;
import parquet.hadoop.mapred.Container;


/**
 * A converter for Parquet-related types.
 * @param <K>
 * @param <V>
 */

public class DefaultParquetTypeConverter<K,V>  implements HadoopTypeConverter<K,V> {


    @Override
    public void convert(Record stratosphereRecord, K hadoopKey, V hadoopValue) {
        stratosphereRecord.setField(0, NullValue.getInstance()); //Parquet always returns null as a key.
        stratosphereRecord.setField(1, convert(hadoopValue));
    }

    private Value convert(Object parquetType) {

        //The container is a wrapper for types, used when working with its deprecated mapred InputFormat.
       if (parquetType instanceof Container) {
            Object value = ((Container)parquetType).get();
            return convert(value); //Converting the content of the wrapper, which is what we need.
       }

       if (parquetType instanceof SimpleGroup) { //An example schema which may be found in parquet-column.

            //"column_name: content_of_column
            String groupStringWithSchema = parquetType.toString();

           /* Isolating the content of the column from the schema information.
            The cleanest (and correct) thing to do would be to map this schema type to a Stratosphere type.
            */
            String groupStringNoSchema = groupStringWithSchema.replaceAll("\n.*?: ", "\n").replaceFirst(".*?: ", "");
            return new StringValue(groupStringNoSchema);
       }
        //TODO Primitives of Parquet such as INT64 should also be converted as they are not considered Hadoop Writables.

        throw new RuntimeException("Unable to convert Parquet type ("+parquetType.getClass().getCanonicalName()+") to Stratosphere.");
    }
}