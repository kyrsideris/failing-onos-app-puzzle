/*
 * Copyright 2014 Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.foo.bar;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onosproject.core.CoreService;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.flow.FlowRuleService;
import org.onosproject.net.host.HostService;

/**
 * Set of tests of the ONOS application component.
 */
public class AppComponentTest {

    private AppComponent component;
    HostService hostService = new MockHostService();
    FlowRuleService flowRuleService = new MockFlowRuleService();
    DeviceService deviceService = new MockDeviceService();
    CoreService coreService = new MockCoreService();

    @Before
    public void setUp() {
        component = new AppComponent();
        component.hostService = hostService;
        component.flowRuleService = flowRuleService;
        component.deviceService = deviceService;
        component.coreService = coreService;
        component.activate();
    }

    @After
    public void tearDown() {
        component.deactivate();
    }

    @Test
    public void basics() {

    }

}
