package jorgedizpico.level;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Random;

import weka.clusterers.DensityBasedClusterer;
import weka.core.Instance;

import jorgedizpico.auto.Executor;
import jorgedizpico.auto.Trace;
import jorgedizpico.cluster.Filters;

import dk.itu.mario.MarioInterface.GamePlay;
import dk.itu.mario.MarioInterface.LevelGenerator;
import dk.itu.mario.MarioInterface.LevelInterface;
import dk.itu.mario.level.Level;

public class LakituLevelGenerator implements LevelGenerator {
	
	public static String clusterFile = "data/cluster.dat";
	
  	protected static Random rand = new Random();
  	protected LakituLevel lvl;
	
	@Override
	public LevelInterface generateLevel(GamePlay playerMetrics) {
		try {
						
			DensityBasedClusterer cl = readClusters(clusterFile);
			Instance inst = makeInstance(playerMetrics);
			double[] clusters = cl.logDensityPerClusterForInstance(inst);

			int type;
			
			switch (weka.core.Utils.maxIndex(clusters)) {
				case 0: // intermediate
					type = Level.TYPE_OVERGROUND;
					break;
				case 1: // speeder
					type = Level.TYPE_CASTLE;
					break;
				default: // explorer
					type = Level.TYPE_UNDERGROUND;
					break;
			}
			
			
			Executor exec = new Executor();
			// 320 blocks length for the level
			// start and end platform are 10 each
			// and chunks are 2 each
			// [320 - (10+10)] / 2 = 150
			Trace trace = exec.generateTraceMix(150, clusters);
			LakituLevel lvl = trace.buildLevel(type);
			
			if (null == lvl)
				throw new Exception("Error while building level from genes.");
			
			return lvl;
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public LevelInterface generateLevel(String detailedInfo) {
		return null;
	}
	
	public DensityBasedClusterer readClusters(String fileName){
		FileInputStream fis = null;
	    ObjectInputStream in = null;
	    DensityBasedClusterer cl =  null;
		try {
			fis = new FileInputStream(fileName);
			in = new ObjectInputStream(fis);
			cl = (DensityBasedClusterer)in.readObject();
			fis.close();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cl;
	}

	protected Instance makeInstance(GamePlay gp) {
		try {

			Double d = 0.0;
			int k = 0;
			Field[] flds = GamePlay.class.getFields();
			Instance inst = new Instance(flds.length - Filters.numSkippedFields());
			Arrays.sort(flds, new Filters().new FieldComparator());
			
			for (int i = 0; i < flds.length; i++) {
				
				if (Filters.isSkippedField(flds[i].getName()))
					continue;
				
				if (flds[i].getType() == Integer.TYPE)
					d = new Double((Integer)flds[i].get(gp));
				else if (flds[i].getType() == Double.TYPE)
					d = (Double) flds[i].get(gp);
				
				inst.setValue(k, d);
				k++;
			}
			
			return inst;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
	}
	
}
