package chapter3.minicluster;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hdfs.tools.GetConf;
import org.apache.hadoop.mapred.MiniMRClientCluster;
import org.apache.hadoop.mapred.MiniMRClientClusterFactory;
import org.apache.hadoop.mapreduce.Counters;
import org.apache.hadoop.mapreduce.Job;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import chapter3.WordCountWithTools;

/**
 * 
 * This test is based on the
 * https://svn.apache.org/repos/asf/hadoop/common/trunk
 * /hadoop-mapreduce-project/
 * hadoop-mapreduce-client/hadoop-mapreduce-client-jobclient
 * /src/test/java/org/apache/hadoop/mapred/TestMiniMRClientCluster.java
 */
public class WordCountMiniClusterTest {
	private static MiniMRClientCluster mrCluster;

	private class InternalClass {
	}

	@BeforeClass
	public static void setup() throws IOException {
		// create the mini cluster to be used for the tests
		mrCluster = MiniMRClientClusterFactory.create(InternalClass.class, 1,
				new Configuration());
	}

	@AfterClass
	public static void cleanup() throws IOException {
		// stopping the mini cluster
		mrCluster.stop();
	}

	@Test
	public void testSimpleMSISDNSum() throws Exception {
		String outDirString = "build/word-count-test";
		String testResDir = "test-resources";
		String testInput = testResDir + "/wc-input.txt";

		// clean output dir
		File outDir = new File(outDirString);
		FileUtils.deleteDirectory(outDir);

		Job job = (new WordCountWithTools()).prepareJob(testInput,
				outDirString, mrCluster.getConfig());
		assertTrue(job.waitForCompletion(true));
		validateCounters(job.getCounters(), 12, 367, 201, 201);
	}

	private void validateCounters(Counters counters, long mapInputRecords,
			long mapOutputRecords, long reduceInputGroups,
			long reduceOutputRecords) {
		assertEquals("MapInputRecords", mapInputRecords,
				counters.findCounter("org.apache.hadoop.mapred.Task$Counter", "MAP_INPUT_RECORDS")
						.getValue());
		assertEquals("MapOutputRecords", mapOutputRecords, counters
				.findCounter("org.apache.hadoop.mapred.Task$Counter", "MAP_OUTPUT_RECORDS").getValue());
		assertEquals("ReduceInputGroups", reduceInputGroups, counters
				.findCounter("org.apache.hadoop.mapred.Task$Counter", "REDUCE_INPUT_GROUPS")
				.getValue());
		assertEquals("ReduceOutputRecords", reduceOutputRecords, counters
				.findCounter("org.apache.hadoop.mapred.Task$Counter", "REDUCE_OUTPUT_RECORDS")
				.getValue());
	}
}
