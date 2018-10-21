package net.rashack.onlineregistry;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ TestCluster.class, TestRegistryCreator.class })
public class IntegrationTestSuite {
}
