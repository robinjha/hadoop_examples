package com.mapreduce.wordcount;

import java.io.IOException;
import java.util.StringTokenizer;
 
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
 
public class HadoopExampleOne extends Configured implements Tool {
 
    public static class Map extends
            Mapper<LongWritable, Text, Text, IntWritable> {
 
        private final static IntWritable one = new IntWritable(1);
 
        @Override
        public void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {
 
            Text word = new Text();
 
            String line = value.toString();
            StringTokenizer tokenizer = new StringTokenizer(line);
            while (tokenizer.hasMoreTokens()) {
                word.set(tokenizer.nextToken());
                context.write(word, one);
            }
        }
    }
 
    public static class Reduce extends
            Reducer<Text, IntWritable, Text, IntWritable> {
 
        @Override
        public void reduce(Text key, Iterable<IntWritable> values,
                Context context) throws IOException, InterruptedException {
            int sum = 0;
            for (IntWritable value : values) {
                sum += value.get();
            }
            context.write(key, new IntWritable(sum));
        }
    }
 
    public static void main(String[] args) throws Exception {
 
        String[] argsLocal = {"input_data", "output_data"};
        Configuration configuration = new Configuration();
        int rc = ToolRunner.run(configuration,
                        new HadoopExampleOne(), argsLocal);
        System.exit(rc);
 
    }
 
    public int run(String[] args) throws Exception {
 
        Job job = new Job();
 
        job.setJarByClass(HadoopExampleOne.class);
        job.setJobName("wordcount");
 
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
 
        job.setMapperClass(Map.class);
        job.setCombinerClass(Reduce.class);
        job.setReducerClass(Reduce.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
 
        job.submit();
 
        int rc = (job.waitForCompletion(true)) ? 1 : 0;
        return rc;
    }
 
}