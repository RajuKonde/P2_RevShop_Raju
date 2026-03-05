package com.revshop;

import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeEngines({"junit-jupiter", "junit-vintage"})
@SelectPackages("com.revshop")
public class RevshopApplicationTests {
}
