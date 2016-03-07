# failing-onos-app-puzzle
History of tries to solve the failing onos application puzzle. Apache Kafka is used as external dependency. Give it a try!

## Preperation
One onos v1.4.0 instance is running in 10.1.1.15 (env variable `$OCI`). The command karaf prompt will be denoted as `onos>`. The building machine runs on 10.1.1.13 which also runs the mininet (env variable `$OCN`)

	ONOS_USER=user
	ONOS_NIC=10.1.1.*
	ONOS_APPS=drivers,openflow,fwd,proxyarp,mobility
	ONOS_FEATURES=webconsole,onos-api,onos-core,onos-cli,onos-openflow,onos-gui,onos-app-fwd,onos-app-proxyarp,onos-app-tvue
	ONOS_GROUP=user
	ONOS_INSTANCES=10.1.1.15
	OC1=10.1.1.15
	OCN=10.1.1.13
	OCI=10.1.1.15

The ONOS application was created with onos-bundle-archetype via the `onos-create-app` script.

Browse the code at the end of this stage here: [ONOS bundle archetype output.](https://github.com/kyrsideris/failing-onos-app-puzzle/tree/c837852013a1e31ce307c07532f102ec323a8659)


<pre><code>

$ onos-create-app app com.foo bar 1.0-SNAPSHOT com.foo.bar -DinteractiveMode=false

	[INFO] Scanning for projects...
	[INFO]                                                                         
	[INFO] ------------------------------------------------------------------------
	[INFO] Building Maven Stub Project (No POM) 1
	[INFO] ------------------------------------------------------------------------
	[INFO] 
	[INFO] >>> maven-archetype-plugin:2.4:generate (default-cli) > generate-sources @ standalone-pom >>>
	[INFO] 
	[INFO] <<< maven-archetype-plugin:2.4:generate (default-cli) < generate-sources @ standalone-pom <<<
	[INFO] 
	[INFO] --- maven-archetype-plugin:2.4:generate (default-cli) @ standalone-pom ---
	[INFO] Generating project in Batch mode
	[INFO] Archetype repository not defined. Using the one from [org.onosproject:onos-bundle-archetype:1.4.0] found in catalog remote
	[INFO] ----------------------------------------------------------------------------
	[INFO] Using following parameters for creating project from Archetype: onos-bundle-archetype:1.4.0
	[INFO] ----------------------------------------------------------------------------
	[INFO] Parameter: groupId, Value: com.foo
	[INFO] Parameter: artifactId, Value: bar
	[INFO] Parameter: version, Value: 1.0-SNAPSHOT
	[INFO] Parameter: package, Value: com.foo.bar
	[INFO] Parameter: packageInPathFormat, Value: com/foo/bar
	[INFO] Parameter: package, Value: com.foo.bar
	[INFO] Parameter: version, Value: 1.0-SNAPSHOT
	[INFO] Parameter: groupId, Value: com.foo
	[INFO] Parameter: artifactId, Value: bar
	[INFO] project created from Archetype in dir: /workspace/bar
	[INFO] ------------------------------------------------------------------------
	[INFO] BUILD SUCCESS
	[INFO] ------------------------------------------------------------------------
	[INFO] Total time: 5.011 s
	[INFO] Finished at: 2016-03-07T09:46:34+00:00
	[INFO] Final Memory: 14M/173M
	[INFO] ------------------------------------------------------------------------

</code></pre>


## First Try


AppComponent is populated with for references with cardinality `MANDATORY_UNARY`. Apacke Kafka library is imported and public class `MyKafkaProducer` is created without instantiating it anywhere in the code. Dependency is added to `pom.xml` for Apache Kafka bundle from servicemix:

```
    <dependency>
	    <groupId>org.apache.servicemix.bundles</groupId>
	    <artifactId>org.apache.servicemix.bundles.kafka-clients</artifactId>
	    <version>0.8.2.2_1</version>
	</dependency>
```
Maven bundle plugin is instructed to export `org.apache.kafka.*`:

```
<plugin>
    <groupId>org.apache.felix</groupId>
    <artifactId>maven-bundle-plugin</artifactId>
    <version>2.5.3</version>
    <extensions>true</extensions>
    <configuration>
        <instructions>
            <Import-Package>net.jpountz*;version="[1.2.0,1.3.0)";resolution:=optional,
                javax.management*,
                org.slf4j*;resolution:=optional,
                org.xerial.snappy;resolution:=optional,
                sun.misc;resolution:=optional,
                sun.nio.ch;resolution:=optional
            </Import-Package>
            <Export-Package>
                org.apache.kafka.*
            </Export-Package>
        </instructions>
    </configuration>
</plugin>
```
I am not sure about the `Import-Package` and `resolution:=optional` part, I got this idea from here: [kafka-clients-0.8.2.2/pom.xml#L45](https://github.com/apache/servicemix-bundles/blob/master/kafka-clients-0.8.2.2/pom.xml#L45)

The result of this stage is an application that builds and gets installed without any problem/complain. Nothing complains through activation except Felix which complains for `UNSATISFIED` application. More details are shown below. It seems that the only Service that gets bounded is `HostService` whereas the `FlowRuleService`, `DeviceService` and `CoreService` are left unbound. In my full version app I can confirm that the app is not working properly.

Browse the code at the end of this stage here: [1st try. No features.xml or app.xml. Application is 'unsatisfied'.](https://github.com/kyrsideris/failing-onos-app-puzzle/tree/b012156884cc12053fc672ac5aba7d7b41870f21)

<pre><code>
$ mvn clean install

	[INFO] Scanning for projects...
	[INFO]                                                                         
	[INFO] ------------------------------------------------------------------------
	[INFO] Building bar 1.0-SNAPSHOT
	[INFO] ------------------------------------------------------------------------
	[INFO] 
	[INFO] --- maven-clean-plugin:2.5:clean (default-clean) @ bar ---
	[INFO] Deleting /workspace/bar/target
	[INFO] 
	[INFO] --- onos-maven-plugin:1.5:swagger (swagger) @ bar ---
	[INFO] 
	[INFO] --- onos-maven-plugin:1.5:cfg (cfg) @ bar ---
	[INFO] Generating ONOS component configuration catalogues...
	[INFO] 
	[INFO] --- maven-resources-plugin:2.7:resources (default-resources) @ bar ---
	[WARNING] Using platform encoding (UTF-8 actually) to copy filtered resources, i.e. build is platform dependent!
	[INFO] skip non existing resourceDirectory /workspace/bar/src/main/resources
	[INFO] 
	[INFO] --- maven-compiler-plugin:2.5.1:compile (default-compile) @ bar ---
	[WARNING] File encoding has not been set, using platform encoding UTF-8, i.e. build is platform dependent!
	[INFO] Compiling 1 source file to /workspace/bar/target/classes
	[INFO] 
	[INFO] --- maven-scr-plugin:1.20.0:scr (generate-scr-srcdescriptor) @ bar ---
	[INFO] Writing 1 Service Component Descriptors to /workspace/bar/target/classes/OSGI-INF/com.foo.bar.AppComponent.xml
	[INFO] 
	[INFO] --- maven-resources-plugin:2.7:testResources (default-testResources) @ bar ---
	[WARNING] Using platform encoding (UTF-8 actually) to copy filtered resources, i.e. build is platform dependent!
	[INFO] skip non existing resourceDirectory /workspace/bar/src/test/resources
	[INFO] 
	[INFO] --- maven-compiler-plugin:2.5.1:testCompile (default-testCompile) @ bar ---
	[WARNING] File encoding has not been set, using platform encoding UTF-8, i.e. build is platform dependent!
	[INFO] Compiling 5 source files to /workspace/bar/target/test-classes
	[INFO] 
	[INFO] --- maven-surefire-plugin:2.19.1:test (default-test) @ bar ---
	
	-------------------------------------------------------
	 T E S T S
	-------------------------------------------------------
	Running com.foo.bar.AppComponentTest
	SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
	SLF4J: Defaulting to no-operation (NOP) logger implementation
	SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
	Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.114 sec - in com.foo.bar.AppComponentTest
	
	Results :
	
	Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
	
	[INFO] 
	[INFO] --- maven-bundle-plugin:2.5.3:bundle (default-bundle) @ bar ---
	[INFO] 
	[INFO] --- onos-maven-plugin:1.5:app (app) @ bar ---
	[INFO] Building ONOS application package for com.foo.app (v1.0-SNAPSHOT)
	[INFO] 
	[INFO] --- maven-install-plugin:2.5.2:install (default-install) @ bar ---
	[INFO] Installing /workspace/bar/target/bar-1.0-SNAPSHOT.jar to /home/kyriakos/.m2/repository/com/foo/bar/1.0-SNAPSHOT/bar-1.0-SNAPSHOT.jar
	[INFO] Installing /workspace/bar/pom.xml to /home/kyriakos/.m2/repository/com/foo/bar/1.0-SNAPSHOT/bar-1.0-SNAPSHOT.pom
	[INFO] Installing /workspace/bar/target/bar-1.0-SNAPSHOT.oar to /home/kyriakos/.m2/repository/com/foo/bar/1.0-SNAPSHOT/bar-1.0-SNAPSHOT.oar
	[INFO] 
	[INFO] --- maven-bundle-plugin:2.5.3:install (default-install) @ bar ---
	[INFO] Installing com/foo/bar/1.0-SNAPSHOT/bar-1.0-SNAPSHOT.jar
	[INFO] Writing OBR metadata
	[INFO] ------------------------------------------------------------------------
	[INFO] BUILD SUCCESS
	[INFO] ------------------------------------------------------------------------
	[INFO] Total time: 4.196 s
	[INFO] Finished at: 2016-03-07T10:46:18+00:00
	[INFO] Final Memory: 24M/250M
	[INFO] ------------------------------------------------------------------------

### Before app installation:

onos> scr:list

	-1  | DISABLED | org.onosproject.net.flowobjective.impl.composition.FlowObjectiveCompositionManager
	-1  | DISABLED | org.onosproject.store.link.impl.GossipLinkStore                                   
	-1  | DISABLED | org.onosproject.store.device.impl.ECDeviceStore                                   
	-1  | DISABLED | org.onosproject.store.cluster.messaging.impl.IOLoopMessagingManager               
	5   | ACTIVE   | org.onosproject.net.intent.impl.compiler.PathIntentCompiler                       
	6   | ACTIVE   | org.onosproject.event.impl.CoreEventDispatcher                                    
	7   | ACTIVE   | org.onosproject.net.topology.impl.DefaultTopologyProvider                         
	8   | ACTIVE   | org.onosproject.net.intent.impl.compiler.LinkCollectionIntentCompiler             
	9   | ACTIVE   | org.onosproject.net.topology.impl.TopologyManager                                 
	10  | ACTIVE   | org.onosproject.net.intent.impl.compiler.OpticalPathIntentCompiler                
	11  | ACTIVE   | org.onosproject.net.intent.impl.compiler.MultiPointToSinglePointIntentCompiler    
	12  | ACTIVE   | org.onosproject.net.intent.impl.IntentManager                                     
	13  | ACTIVE   | org.onosproject.net.packet.impl.PacketManager                                     
	14  | ACTIVE   | org.onosproject.net.topology.impl.PathManager                                     
	15  | ACTIVE   | org.onosproject.net.intent.impl.compiler.OpticalConnectivityIntentCompiler        
	16  | ACTIVE   | org.onosproject.net.config.impl.NetworkConfigLoader                               
	17  | ACTIVE   | org.onosproject.net.config.impl.BasicNetworkConfigs                               
	18  | ACTIVE   | org.onosproject.cfg.impl.ComponentConfigLoader                                    
	19  | ACTIVE   | org.onosproject.net.resource.impl.LinkResourceManager                             
	20  | ACTIVE   | org.onosproject.cluster.impl.ClusterMetadataManager                               
	21  | ACTIVE   | org.onosproject.net.intent.impl.compiler.PointToPointIntentCompiler               
	22  | ACTIVE   | org.onosproject.net.intent.impl.compiler.TwoWayP2PIntentCompiler                  
	23  | ACTIVE   | org.onosproject.net.flow.impl.FlowRuleManager                                     
	24  | ACTIVE   | org.onosproject.net.newresource.impl.ResourceManager                              
	25  | ACTIVE   | org.onosproject.net.intent.impl.compiler.MplsPathIntentCompiler                   
	26  | ACTIVE   | org.onosproject.net.intent.impl.compiler.OpticalCircuitIntentCompiler             
	27  | ACTIVE   | org.onosproject.net.device.impl.DeviceManager                                     
	28  | ACTIVE   | org.onosproject.cluster.impl.MastershipManager                                    
	29  | ACTIVE   | org.onosproject.net.link.impl.LinkManager                                         
	30  | ACTIVE   | org.onosproject.net.statistic.impl.FlowStatisticManager                           
	31  | ACTIVE   | org.onosproject.core.impl.CoreManager                                             
	32  | ACTIVE   | org.onosproject.net.newresource.impl.ResourceRegistrar                            
	33  | ACTIVE   | org.onosproject.net.intent.impl.compiler.MplsIntentCompiler                       
	34  | ACTIVE   | org.onosproject.net.config.impl.NetworkConfigManager                              
	35  | ACTIVE   | org.onosproject.net.group.impl.GroupManager                                       
	36  | ACTIVE   | org.onosproject.net.intent.impl.compiler.HostToHostIntentCompiler                 
	37  | ACTIVE   | org.onosproject.net.intent.impl.IntentCleanup                                     
	38  | ACTIVE   | org.onosproject.core.impl.MetricsManagerComponent                                 
	39  | ACTIVE   | org.onosproject.net.driver.impl.DriverManager                                     
	40  | ACTIVE   | org.onosproject.net.statistic.impl.StatisticManager                               
	41  | ACTIVE   | org.onosproject.net.edgeservice.impl.EdgeManager                                  
	42  | ACTIVE   | org.onosproject.net.intent.impl.ObjectiveTracker                                  
	43  | ACTIVE   | org.onosproject.net.intent.impl.compiler.SinglePointToMultiPointIntentCompiler    
	44  | ACTIVE   | org.onosproject.cluster.impl.ClusterManager                                       
	45  | ACTIVE   | org.onosproject.net.proxyarp.impl.ProxyArpManager                                 
	46  | ACTIVE   | org.onosproject.cfg.impl.ComponentConfigManager                                   
	47  | ACTIVE   | org.onosproject.net.flowobjective.impl.FlowObjectiveManager                       
	48  | ACTIVE   | org.onosproject.app.impl.ApplicationManager                                       
	49  | ACTIVE   | org.onosproject.net.host.impl.HostManager                                         
	50  | ACTIVE   | org.onosproject.codec.impl.CodecManager                                           
	51  | ACTIVE   | org.onosproject.store.cluster.impl.StaticClusterMetadataStore                     
	52  | ACTIVE   | org.onosproject.store.topology.impl.DistributedTopologyStore                      
	53  | ACTIVE   | org.onosproject.store.mastership.impl.ConsistentDeviceMastershipStore             
	54  | ACTIVE   | org.onosproject.store.consistent.impl.DatabaseManager                             
	55  | ACTIVE   | org.onosproject.store.statistic.impl.DistributedFlowStatisticStore                
	56  | ACTIVE   | org.onosproject.store.core.impl.LogicalClockManager                               
	57  | ACTIVE   | org.onosproject.store.flow.impl.ReplicaInfoManager                                
	58  | ACTIVE   | org.onosproject.store.core.impl.ConsistentApplicationIdStore                      
	59  | ACTIVE   | org.onosproject.store.resource.impl.ConsistentIntentSetMultimap                   
	60  | ACTIVE   | org.onosproject.store.group.impl.DistributedGroupStore                            
	61  | ACTIVE   | org.onosproject.store.cluster.messaging.impl.NettyMessagingManager                
	62  | ACTIVE   | org.onosproject.store.app.GossipApplicationStore                                  
	63  | ACTIVE   | org.onosproject.store.flow.impl.NewDistributedFlowRuleStore                       
	64  | ACTIVE   | org.onosproject.store.flowobjective.impl.DistributedFlowObjectiveStore            
	65  | ACTIVE   | org.onosproject.store.consistent.impl.MutexExecutionManager                       
	66  | ACTIVE   | org.onosproject.store.intent.impl.GossipIntentStore                               
	67  | ACTIVE   | org.onosproject.store.consistent.impl.DistributedLeadershipManager                
	68  | ACTIVE   | org.onosproject.store.host.impl.DistributedHostStore                              
	69  | ACTIVE   | org.onosproject.store.newresource.impl.ConsistentResourceStore                    
	70  | ACTIVE   | org.onosproject.store.config.impl.DistributedNetworkConfigStore                   
	71  | ACTIVE   | org.onosproject.store.device.impl.GossipDeviceStore                               
	72  | ACTIVE   | org.onosproject.store.resource.impl.ConsistentLinkResourceStore                   
	73  | ACTIVE   | org.onosproject.store.cluster.messaging.impl.ClusterCommunicationManager          
	74  | ACTIVE   | org.onosproject.store.device.impl.DeviceClockManager                              
	75  | ACTIVE   | org.onosproject.store.link.impl.ECLinkStore                                       
	76  | ACTIVE   | org.onosproject.store.cluster.impl.DistributedClusterStore                        
	77  | ACTIVE   | org.onosproject.store.cfg.GossipComponentConfigStore                              
	78  | ACTIVE   | org.onosproject.store.proxyarp.impl.DistributedProxyArpStore                      
	79  | ACTIVE   | org.onosproject.store.intent.impl.PartitionManager                                
	80  | ACTIVE   | org.onosproject.store.statistic.impl.DistributedStatisticStore                    
	81  | ACTIVE   | org.onosproject.store.core.impl.ConsistentIdBlockStore                            
	82  | ACTIVE   | org.onosproject.store.packet.impl.DistributedPacketStore                          
	83  | ACTIVE   | org.onosproject.persistence.impl.PersistenceManager                               
	84  | ACTIVE   | org.onosproject.incubator.net.virtual.impl.VirtualNetworkManager                  
	85  | ACTIVE   | org.onosproject.incubator.net.tunnel.impl.TunnelManager                           
	86  | ACTIVE   | org.onosproject.incubator.net.meter.impl.MeterManager                             
	87  | ACTIVE   | org.onosproject.incubator.net.config.impl.ExtraNetworkConfigs                     
	88  | ACTIVE   | org.onosproject.incubator.net.mcast.impl.MulticastRouteManager                    
	89  | ACTIVE   | org.onosproject.incubator.net.impl.PortStatisticsManager                          
	90  | ACTIVE   | org.onosproject.incubator.net.intf.impl.InterfaceManager                          
	91  | ACTIVE   | org.onosproject.incubator.net.domain.impl.IntentDomainManager                     
	92  | ACTIVE   | org.onosproject.incubator.net.resource.label.impl.LabelResourceManager            
	93  | ACTIVE   | org.onosproject.incubator.store.meter.impl.DistributedMeterStore                  
	94  | ACTIVE   | org.onosproject.incubator.store.tunnel.impl.DistributedTunnelStore                
	95  | ACTIVE   | org.onosproject.incubator.store.virtual.impl.DistributedVirtualNetworkStore       
	96  | ACTIVE   | org.onosproject.incubator.store.resource.impl.DistributedLabelResourceStore       
	97  | ACTIVE   | org.onosproject.incubator.rpc.impl.LocalRemoteServiceProvider                     
	98  | ACTIVE   | org.onosproject.incubator.rpc.impl.RemoteServiceManager                           
	99  | ACTIVE   | org.onosproject.cli.CliComponent                                                  
	100 | ACTIVE   | org.onosproject.rest.impl.ApiDocRegistrator                                       
	101 | ACTIVE   | org.onosproject.rest.impl.ApiDocManager                                           
	102 | ACTIVE   | org.onosproject.ui.impl.UiExtensionManager                                        
	103 | ACTIVE   | org.onosproject.openflow.controller.impl.OpenFlowControllerImpl                   
	104 | ACTIVE   | org.onosproject.provider.of.device.impl.OpenFlowDeviceProvider                    
	105 | ACTIVE   | org.onosproject.provider.of.packet.impl.OpenFlowPacketProvider                    
	106 | ACTIVE   | org.onosproject.provider.of.flow.impl.OpenFlowRuleProvider                        
	107 | ACTIVE   | org.onosproject.provider.of.group.impl.OpenFlowGroupProvider                      
	108 | ACTIVE   | org.onosproject.provider.of.meter.impl.OpenFlowMeterProvider                      
	109 | ACTIVE   | org.onosproject.provider.host.impl.HostLocationProvider                           
	110 | ACTIVE   | org.onosproject.provider.lldp.impl.LldpLinkProvider                               
	111 | ACTIVE   | org.onosproject.fwd.ReactiveForwarding                                            
	112 | ACTIVE   | org.onosproject.driver.DefaultDrivers                                             
	113 | ACTIVE   | org.onosproject.proxyarp.ProxyArp  


onos> apps -s

	*   1 org.onosproject.openflow-base    1.4.0    OpenFlow protocol southbound providers
	*   2 org.onosproject.hostprovider     1.4.0    ONOS host location provider
	*   3 org.onosproject.lldpprovider     1.4.0    ONOS LLDP link provider
	*   4 org.onosproject.openflow         1.4.0    OpenFlow southbound meta application
	    5 org.onosproject.election         1.4.0    Master election test application
	    6 org.onosproject.openstackswitching 1.4.0    SONA Openstack Switching  applications
	    7 org.onosproject.bgp              1.4.0    BGP protocol southbound providers
	    8 org.onosproject.incubator.rpc    1.4.0    ONOS inter-cluster RPC service
	    9 org.onosproject.cip              1.4.0    Cluster IP alias
	   10 org.onosproject.aaa              1.4.0    ONOS authentication application
	   11 org.onosproject.netconf          1.4.0    NetConf protocol southbound providers
	   12 org.onosproject.reactive.routing 1.4.0    SDN-IP reactive routing application
	   13 org.onosproject.flowanalyzer     1.4.0    Simple flow space analyzer
	   14 org.onosproject.mobility         1.4.0    Host mobility application
	   15 org.onosproject.messagingperf    1.4.0    Messaging performance test application
	   16 org.onosproject.vtn              1.4.0    ONOS framework applications
	   17 org.onosproject.netcfghostprovider 1.4.0    Host provider that uses network config service to discover hosts.
	   18 org.onosproject.drivermatrix     1.4.0    Driver behaviour support matric
	   19 org.onosproject.cordfabric       1.4.0    Simple fabric application for CORD
	*  20 org.onosproject.fwd              1.4.0    Reactive forwarding application using flow subsystem
	   21 org.onosproject.igmp             1.4.0    Internet Group Message Protocol
	   22 org.onosproject.pathpainter      1.4.0    Path visualization application
	*  23 org.onosproject.drivers          1.4.0    Builtin device drivers
	   24 org.onosproject.distributedprimitives 1.4.0    ONOS app to test distributed primitives
	   25 org.onosproject.olt              1.4.0    OLT application
	   26 org.onosproject.intentperf       1.4.0    Intent performance test application
	   27 org.onosproject.ovsdb            1.4.0    ONOS information providers and control/management protocol adapter
	   28 org.onosproject.cordvtn          1.4.0    Virtual tenant network service for CORD
	   29 org.onosproject.incubator.rpc.grpc 1.4.0    ONOS inter-cluster RPC based on gRPC
	*  30 org.onosproject.proxyarp         1.4.0    Proxy ARP/NDP application
	   31 org.onosproject.optical          1.4.0    Packet/Optical use-case application
	   32 org.onosproject.mfwd             1.4.0    Multicast forwarding application
	   33 org.onosproject.dhcp             1.4.0    DHCP Server application
	   34 org.onosproject.sdnip            1.4.0    SDN-IP peering application
	   35 org.onosproject.xosintegration   1.4.0    ONOS XOS integration application
	   36 org.onosproject.metrics          1.4.0    Performance metrics collection
	   37 org.onosproject.mlb              1.4.0    Balances mastership among nodes
	   38 org.onosproject.faultmanagement  1.4.0    ONOS framework applications
	   39 org.onosproject.pim              1.4.0    Protocol Independent Multicast Emulation
	   40 org.onosproject.bgprouter        1.4.0    BGP router application
	   41 org.onosproject.pcep             1.4.0    PCEP protocol southbound providers
	   42 org.onosproject.demo             1.4.0    Flow throughput test application
	   43 org.onosproject.virtualbng       1.4.0    A virtual Broadband Network Gateway(BNG) application
	   44 org.onosproject.segmentrouting   1.4.0    Segment routing application
	   45 org.onosproject.acl              1.4.0    ONOS ACL application
	   46 org.onosproject.null             1.4.0    Null southbound providers


onos> log:clear

### App installation:

$ onos-app $OCI install! target/bar-1.0-SNAPSHOT.oar 

	{"name":"org.foo.app","id":53,"version":"1.0.SNAPSHOT","description":"ONOS OSGi bundle archetype","origin":"Foo, Inc.","permissions":"[]","featuresRepo":"mvn:com.foo/bar/1.0-SNAPSHOT/xml/features","features":"[bar]","requiredApps":"[]","state":"ACTIVE"}


onos> log:display

	2016-03-07 10:48:32,082 | INFO  | qtp103630005-66  | ApplicationManager               | 83 - org.onosproject.onos-core-net - 1.4.0 | Application com.foo.app has been installed
	2016-03-07 10:48:32,097 | INFO  | qtp103630005-66  | FeaturesServiceImpl              | 20 - org.apache.karaf.features.core - 3.0.5 | Installing feature bar 1.0-SNAPSHOT
	2016-03-07 10:48:32,098 | INFO  | qtp103630005-66  | FeaturesServiceImpl              | 20 - org.apache.karaf.features.core - 3.0.5 | Found installed feature onos-api 1.4.0
	2016-03-07 10:48:32,156 | INFO  | qtp103630005-66  | FeaturesServiceImpl              | 20 - org.apache.karaf.features.core - 3.0.5 | Found installed feature scr-condition-management_0_0_0 3.0.5
	2016-03-07 10:48:32,156 | INFO  | qtp103630005-66  | FeaturesServiceImpl              | 20 - org.apache.karaf.features.core - 3.0.5 | Found installed feature scr-condition-webconsole_0_0_0 3.0.5
	2016-03-07 10:48:32,159 | INFO  | qtp103630005-66  | FeaturesServiceImpl              | 20 - org.apache.karaf.features.core - 3.0.5 | Found installed feature standard-condition-webconsole_0_0_0 3.0.5
	2016-03-07 10:48:32,160 | INFO  | qtp103630005-66  | FeaturesServiceImpl              | 20 - org.apache.karaf.features.core - 3.0.5 | Found installed feature webconsole-condition-scr_0_0_0 3.0.5
	2016-03-07 10:48:32,191 | INFO  | qtp103630005-66  | ApplicationManager               | 83 - org.onosproject.onos-core-net - 1.4.0 | Application com.foo.app has been activated


onos> scr:list

	ID  | State       | Component Name                                                                    
	------------------------------------------------------------------------------------------------------
	-1  | DISABLED    | org.onosproject.net.flowobjective.impl.composition.FlowObjectiveCompositionManager
	-1  | DISABLED    | org.onosproject.store.link.impl.GossipLinkStore                                   
	-1  | DISABLED    | org.onosproject.store.device.impl.ECDeviceStore                                   
	-1  | DISABLED    | org.onosproject.store.cluster.messaging.impl.IOLoopMessagingManager               
	5   | ACTIVE      | org.onosproject.net.intent.impl.compiler.PathIntentCompiler                       
	6   | ACTIVE      | org.onosproject.event.impl.CoreEventDispatcher                                    
	7   | ACTIVE      | org.onosproject.net.topology.impl.DefaultTopologyProvider                         
	8   | ACTIVE      | org.onosproject.net.intent.impl.compiler.LinkCollectionIntentCompiler             
	9   | ACTIVE      | org.onosproject.net.topology.impl.TopologyManager                                 
	10  | ACTIVE      | org.onosproject.net.intent.impl.compiler.OpticalPathIntentCompiler                
	11  | ACTIVE      | org.onosproject.net.intent.impl.compiler.MultiPointToSinglePointIntentCompiler    
	12  | ACTIVE      | org.onosproject.net.intent.impl.IntentManager                                     
	13  | ACTIVE      | org.onosproject.net.packet.impl.PacketManager                                     
	14  | ACTIVE      | org.onosproject.net.topology.impl.PathManager                                     
	15  | ACTIVE      | org.onosproject.net.intent.impl.compiler.OpticalConnectivityIntentCompiler        
	16  | ACTIVE      | org.onosproject.net.config.impl.NetworkConfigLoader                               
	17  | ACTIVE      | org.onosproject.net.config.impl.BasicNetworkConfigs                               
	18  | ACTIVE      | org.onosproject.cfg.impl.ComponentConfigLoader                                    
	19  | ACTIVE      | org.onosproject.net.resource.impl.LinkResourceManager                             
	20  | ACTIVE      | org.onosproject.cluster.impl.ClusterMetadataManager                               
	21  | ACTIVE      | org.onosproject.net.intent.impl.compiler.PointToPointIntentCompiler               
	22  | ACTIVE      | org.onosproject.net.intent.impl.compiler.TwoWayP2PIntentCompiler                  
	23  | ACTIVE      | org.onosproject.net.flow.impl.FlowRuleManager                                     
	24  | ACTIVE      | org.onosproject.net.newresource.impl.ResourceManager                              
	25  | ACTIVE      | org.onosproject.net.intent.impl.compiler.MplsPathIntentCompiler                   
	26  | ACTIVE      | org.onosproject.net.intent.impl.compiler.OpticalCircuitIntentCompiler             
	27  | ACTIVE      | org.onosproject.net.device.impl.DeviceManager                                     
	28  | ACTIVE      | org.onosproject.cluster.impl.MastershipManager                                    
	29  | ACTIVE      | org.onosproject.net.link.impl.LinkManager                                         
	30  | ACTIVE      | org.onosproject.net.statistic.impl.FlowStatisticManager                           
	31  | ACTIVE      | org.onosproject.core.impl.CoreManager                                             
	32  | ACTIVE      | org.onosproject.net.newresource.impl.ResourceRegistrar                            
	33  | ACTIVE      | org.onosproject.net.intent.impl.compiler.MplsIntentCompiler                       
	34  | ACTIVE      | org.onosproject.net.config.impl.NetworkConfigManager                              
	35  | ACTIVE      | org.onosproject.net.group.impl.GroupManager                                       
	36  | ACTIVE      | org.onosproject.net.intent.impl.compiler.HostToHostIntentCompiler                 
	37  | ACTIVE      | org.onosproject.net.intent.impl.IntentCleanup                                     
	38  | ACTIVE      | org.onosproject.core.impl.MetricsManagerComponent                                 
	39  | ACTIVE      | org.onosproject.net.driver.impl.DriverManager                                     
	40  | ACTIVE      | org.onosproject.net.statistic.impl.StatisticManager                               
	41  | ACTIVE      | org.onosproject.net.edgeservice.impl.EdgeManager                                  
	42  | ACTIVE      | org.onosproject.net.intent.impl.ObjectiveTracker                                  
	43  | ACTIVE      | org.onosproject.net.intent.impl.compiler.SinglePointToMultiPointIntentCompiler    
	44  | ACTIVE      | org.onosproject.cluster.impl.ClusterManager                                       
	45  | ACTIVE      | org.onosproject.net.proxyarp.impl.ProxyArpManager                                 
	46  | ACTIVE      | org.onosproject.cfg.impl.ComponentConfigManager                                   
	47  | ACTIVE      | org.onosproject.net.flowobjective.impl.FlowObjectiveManager                       
	48  | ACTIVE      | org.onosproject.app.impl.ApplicationManager                                       
	49  | ACTIVE      | org.onosproject.net.host.impl.HostManager                                         
	50  | ACTIVE      | org.onosproject.codec.impl.CodecManager                                           
	51  | ACTIVE      | org.onosproject.store.cluster.impl.StaticClusterMetadataStore                     
	52  | ACTIVE      | org.onosproject.store.topology.impl.DistributedTopologyStore                      
	53  | ACTIVE      | org.onosproject.store.mastership.impl.ConsistentDeviceMastershipStore             
	54  | ACTIVE      | org.onosproject.store.consistent.impl.DatabaseManager                             
	55  | ACTIVE      | org.onosproject.store.statistic.impl.DistributedFlowStatisticStore                
	56  | ACTIVE      | org.onosproject.store.core.impl.LogicalClockManager                               
	57  | ACTIVE      | org.onosproject.store.flow.impl.ReplicaInfoManager                                
	58  | ACTIVE      | org.onosproject.store.core.impl.ConsistentApplicationIdStore                      
	59  | ACTIVE      | org.onosproject.store.resource.impl.ConsistentIntentSetMultimap                   
	60  | ACTIVE      | org.onosproject.store.group.impl.DistributedGroupStore                            
	61  | ACTIVE      | org.onosproject.store.cluster.messaging.impl.NettyMessagingManager                
	62  | ACTIVE      | org.onosproject.store.app.GossipApplicationStore                                  
	63  | ACTIVE      | org.onosproject.store.flow.impl.NewDistributedFlowRuleStore                       
	64  | ACTIVE      | org.onosproject.store.flowobjective.impl.DistributedFlowObjectiveStore            
	65  | ACTIVE      | org.onosproject.store.consistent.impl.MutexExecutionManager                       
	66  | ACTIVE      | org.onosproject.store.intent.impl.GossipIntentStore                               
	67  | ACTIVE      | org.onosproject.store.consistent.impl.DistributedLeadershipManager                
	68  | ACTIVE      | org.onosproject.store.host.impl.DistributedHostStore                              
	69  | ACTIVE      | org.onosproject.store.newresource.impl.ConsistentResourceStore                    
	70  | ACTIVE      | org.onosproject.store.config.impl.DistributedNetworkConfigStore                   
	71  | ACTIVE      | org.onosproject.store.device.impl.GossipDeviceStore                               
	72  | ACTIVE      | org.onosproject.store.resource.impl.ConsistentLinkResourceStore                   
	73  | ACTIVE      | org.onosproject.store.cluster.messaging.impl.ClusterCommunicationManager          
	74  | ACTIVE      | org.onosproject.store.device.impl.DeviceClockManager                              
	75  | ACTIVE      | org.onosproject.store.link.impl.ECLinkStore                                       
	76  | ACTIVE      | org.onosproject.store.cluster.impl.DistributedClusterStore                        
	77  | ACTIVE      | org.onosproject.store.cfg.GossipComponentConfigStore                              
	78  | ACTIVE      | org.onosproject.store.proxyarp.impl.DistributedProxyArpStore                      
	79  | ACTIVE      | org.onosproject.store.intent.impl.PartitionManager                                
	80  | ACTIVE      | org.onosproject.store.statistic.impl.DistributedStatisticStore                    
	81  | ACTIVE      | org.onosproject.store.core.impl.ConsistentIdBlockStore                            
	82  | ACTIVE      | org.onosproject.store.packet.impl.DistributedPacketStore                          
	83  | ACTIVE      | org.onosproject.persistence.impl.PersistenceManager                               
	84  | ACTIVE      | org.onosproject.incubator.net.virtual.impl.VirtualNetworkManager                  
	85  | ACTIVE      | org.onosproject.incubator.net.tunnel.impl.TunnelManager                           
	86  | ACTIVE      | org.onosproject.incubator.net.meter.impl.MeterManager                             
	87  | ACTIVE      | org.onosproject.incubator.net.config.impl.ExtraNetworkConfigs                     
	88  | ACTIVE      | org.onosproject.incubator.net.mcast.impl.MulticastRouteManager                    
	89  | ACTIVE      | org.onosproject.incubator.net.impl.PortStatisticsManager                          
	90  | ACTIVE      | org.onosproject.incubator.net.intf.impl.InterfaceManager                          
	91  | ACTIVE      | org.onosproject.incubator.net.domain.impl.IntentDomainManager                     
	92  | ACTIVE      | org.onosproject.incubator.net.resource.label.impl.LabelResourceManager            
	93  | ACTIVE      | org.onosproject.incubator.store.meter.impl.DistributedMeterStore                  
	94  | ACTIVE      | org.onosproject.incubator.store.tunnel.impl.DistributedTunnelStore                
	95  | ACTIVE      | org.onosproject.incubator.store.virtual.impl.DistributedVirtualNetworkStore       
	96  | ACTIVE      | org.onosproject.incubator.store.resource.impl.DistributedLabelResourceStore       
	97  | ACTIVE      | org.onosproject.incubator.rpc.impl.LocalRemoteServiceProvider                     
	98  | ACTIVE      | org.onosproject.incubator.rpc.impl.RemoteServiceManager                           
	99  | ACTIVE      | org.onosproject.cli.CliComponent                                                  
	100 | ACTIVE      | org.onosproject.rest.impl.ApiDocRegistrator                                       
	101 | ACTIVE      | org.onosproject.rest.impl.ApiDocManager                                           
	102 | ACTIVE      | org.onosproject.ui.impl.UiExtensionManager                                        
	103 | ACTIVE      | org.onosproject.openflow.controller.impl.OpenFlowControllerImpl                   
	104 | ACTIVE      | org.onosproject.provider.of.device.impl.OpenFlowDeviceProvider                    
	105 | ACTIVE      | org.onosproject.provider.of.packet.impl.OpenFlowPacketProvider                    
	106 | ACTIVE      | org.onosproject.provider.of.flow.impl.OpenFlowRuleProvider                        
	107 | ACTIVE      | org.onosproject.provider.of.group.impl.OpenFlowGroupProvider                      
	108 | ACTIVE      | org.onosproject.provider.of.meter.impl.OpenFlowMeterProvider                      
	109 | ACTIVE      | org.onosproject.provider.host.impl.HostLocationProvider                           
	110 | ACTIVE      | org.onosproject.provider.lldp.impl.LldpLinkProvider                               
	111 | ACTIVE      | org.onosproject.fwd.ReactiveForwarding                                            
	112 | ACTIVE      | org.onosproject.driver.DefaultDrivers                                             
	113 | ACTIVE      | org.onosproject.proxyarp.ProxyArp                                                 
	114 | UNSATISFIED | com.foo.bar.AppComponent  


onos> scr:details com.foo.bar.AppComponent 

	Component Details
	  Name                : com.foo.bar.AppComponent
	  State               : UNSATISFIED
	  Properties          : 
	    service.pid=com.foo.bar.AppComponent
	    component.name=com.foo.bar.AppComponent
	    component.id=114
	References
	  Reference           : hostService
	    State             : satisfied
	    Multiple          : single
	    Optional          : mandatory
	    Policy            : static
	    Service Reference : Bound Service ID 1165 (org.onosproject.net.host.impl.HostManager)
	  Reference           : flowRuleService
	    State             : satisfied
	    Multiple          : single
	    Optional          : mandatory
	    Policy            : static
	    Service Reference : No Services bound
	  Reference           : deviceService
	    State             : satisfied
	    Multiple          : single
	    Optional          : mandatory
	    Policy            : static
	    Service Reference : No Services bound
	  Reference           : coreService
	    State             : satisfied
	    Multiple          : single
	    Optional          : mandatory
	    Policy            : static
	    Service Reference : No Services bound

onos> app uninstall com.foo.app 
onos> log:clear
onos> shutdown -c -cc -h

</code></pre>

## Second Try
Picking Ali's suggestion on defining the `features.xml` file ( [from this similar case](https://groups.google.com/a/onosproject.org/forum/#!msg/onos-dev/5aAs3TjHDIk/v4nR8uPkBwAJ) )

```
<features xmlns="http://karaf.apache.org/xmlns/features/v1.2.0"
    name="${project.groupId}.${project.artifactId}">
    <feature name="${project.artifactId}" version="${project.version}"
             description="${project.description}">

        <bundle>mvn:org.apache.servicemix.bundles.kafka-clients/org.apache.servicemix.bundles.kafka-clients/0.8.2.2_1</bundle>


        <bundle>mvn:${project.groupId}/${project.artifactId}/${project.version}</bundle>
    </feature>
</features>
```

I am now quit sure if Apache Kafka should be loaded 'as part of a feature' as Thomas was suggesting on the same thread: 
( [quote: "for multi-bundle apps, the developer has to specify these instructions explicitly.""](https://groups.google.com/a/onosproject.org/forum/#!msg/onos-dev/5aAs3TjHDIk/v4nR8uPkBwAJ) )


The resulting application builds without problem but the installation/activation fails with `ArtifactResolutionException`. The message suggests that bundles `kafka-clients:jar:0.8.2.2_1` could not be found in system.repository (file:/opt/onos/apache-karaf-3.0.5/system/). Browsing through the OCI's file system I could see the repo:

```
user@OCI:~$ ls /opt/onos/apache-karaf-3.0.5/system/org/apache/servicemix/bundles/org.apache.servicemix.bundles.kafka-clients/0.8.2.2_1/
	org.apache.servicemix.bundles.kafka-clients-0.8.2.2_1.jar
	org.apache.servicemix.bundles.kafka-clients-0.8.2.2_1.jar.sha1
	org.apache.servicemix.bundles.kafka-clients-0.8.2.2_1.pom
	org.apache.servicemix.bundles.kafka-clients-0.8.2.2_1.pom.sha1
	_remote.repositories
```
Moreover, the application is listed as activated in `apps -s`. Felix doesn't report this application as expected.

Browse the code at the end of this stage here: [2nd try. Features.xml but no app.xml. Application fails to get activated.](https://github.com/kyrsideris/failing-onos-app-puzzle/tree/83d22d149f44895e8c9ca530af9a541c0ce37955)

<pre><code>

$ mvn clean install

	[INFO] Scanning for projects...
	[INFO]                                                                         
	[INFO] ------------------------------------------------------------------------
	[INFO] Building bar 1.0-SNAPSHOT
	[INFO] ------------------------------------------------------------------------
	[INFO] 
	[INFO] --- maven-clean-plugin:2.5:clean (default-clean) @ bar ---
	[INFO] 
	[INFO] --- onos-maven-plugin:1.5:swagger (swagger) @ bar ---
	[INFO] 
	[INFO] --- onos-maven-plugin:1.5:cfg (cfg) @ bar ---
	[INFO] Generating ONOS component configuration catalogues...
	[INFO] 
	[INFO] --- maven-resources-plugin:2.7:resources (default-resources) @ bar ---
	[WARNING] Using platform encoding (UTF-8 actually) to copy filtered resources, i.e. build is platform dependent!
	[INFO] skip non existing resourceDirectory /workspace/bar/src/main/resources
	[INFO] 
	[INFO] --- maven-compiler-plugin:2.5.1:compile (default-compile) @ bar ---
	[WARNING] File encoding has not been set, using platform encoding UTF-8, i.e. build is platform dependent!
	[INFO] Compiling 1 source file to /workspace/bar/target/classes
	[INFO] 
	[INFO] --- maven-scr-plugin:1.20.0:scr (generate-scr-srcdescriptor) @ bar ---
	[INFO] Writing 1 Service Component Descriptors to /workspace/bar/target/classes/OSGI-INF/com.foo.bar.AppComponent.xml
	[INFO] 
	[INFO] --- maven-resources-plugin:2.7:testResources (default-testResources) @ bar ---
	[WARNING] Using platform encoding (UTF-8 actually) to copy filtered resources, i.e. build is platform dependent!
	[INFO] skip non existing resourceDirectory /workspace/bar/src/test/resources
	[INFO] 
	[INFO] --- maven-compiler-plugin:2.5.1:testCompile (default-testCompile) @ bar ---
	[WARNING] File encoding has not been set, using platform encoding UTF-8, i.e. build is platform dependent!
	[INFO] Compiling 5 source files to /workspace/bar/target/test-classes
	[INFO] 
	[INFO] --- maven-surefire-plugin:2.19.1:test (default-test) @ bar ---
	
	-------------------------------------------------------
	 T E S T S
	-------------------------------------------------------
	Running com.foo.bar.AppComponentTest
	SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
	SLF4J: Defaulting to no-operation (NOP) logger implementation
	SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
	Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.158 sec - in com.foo.bar.AppComponentTest
	
	Results :
	
	Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
	
	[INFO] 
	[INFO] --- maven-bundle-plugin:2.5.3:bundle (default-bundle) @ bar ---
	[INFO] 
	[INFO] --- onos-maven-plugin:1.5:app (app) @ bar ---
	[INFO] Building ONOS application package for com.foo.app (v1.0-SNAPSHOT)
	[INFO] 
	[INFO] --- maven-install-plugin:2.5.2:install (default-install) @ bar ---
	[INFO] Installing /workspace/bar/target/bar-1.0-SNAPSHOT.jar to /home/kyriakos/.m2/repository/com/foo/bar/1.0-SNAPSHOT/bar-1.0-SNAPSHOT.jar
	[INFO] Installing /workspace/bar/pom.xml to /home/kyriakos/.m2/repository/com/foo/bar/1.0-SNAPSHOT/bar-1.0-SNAPSHOT.pom
	[INFO] Installing /workspace/bar/target/bar-1.0-SNAPSHOT.oar to /home/kyriakos/.m2/repository/com/foo/bar/1.0-SNAPSHOT/bar-1.0-SNAPSHOT.oar
	[INFO] 
	[INFO] --- maven-bundle-plugin:2.5.3:install (default-install) @ bar ---
	[INFO] Installing com/foo/bar/1.0-SNAPSHOT/bar-1.0-SNAPSHOT.jar
	[INFO] Writing OBR metadata
	[INFO] ------------------------------------------------------------------------
	[INFO] BUILD SUCCESS
	[INFO] ------------------------------------------------------------------------
	[INFO] Total time: 4.781 s
	[INFO] Finished at: 2016-03-07T11:06:35+00:00
	[INFO] Final Memory: 25M/322M
	[INFO] ------------------------------------------------------------------------


onos> log:clear

$ onos-app $OCI install! target/bar-1.0-SNAPSHOT.oar

	{"name":"com.foo.app","id":53,"version":"1.0.SNAPSHOT","description":"ONOS OSGi bundle archetype","origin":"Foo, Inc.","permissions":"[]","featuresRepo":"mvn:com.foo/bar/1.0-SNAPSHOT/xml/features","features":"[bar]","requiredApps":"[]","state":"ACTIVE"}


onos> log:display

	2016-03-07 11:15:32,375 | INFO  | qtp1055756261-61 | ApplicationManager               | 83 - org.onosproject.onos-core-net - 1.4.0 | Application com.foo.app has been installed
	2016-03-07 11:15:32,387 | INFO  | qtp1055756261-61 | FeaturesServiceImpl              | 20 - org.apache.karaf.features.core - 3.0.5 | Installing feature bar 1.0-SNAPSHOT
	2016-03-07 11:15:33,770 | WARN  | qtp1055756261-61 | AetherBasedResolver              | 3 - org.ops4j.pax.logging.pax-logging-api - 1.8.4 | Error resolving artifactorg.apache.servicemix.bundles.kafka-clients:org.apache.servicemix.bundles.kafka-clients:jar:0.8.2.2_1:Could not find artifact org.apache.servicemix.bundles.kafka-clients:org.apache.servicemix.bundles.kafka-clients:jar:0.8.2.2_1 in system.repository (file:/opt/onos/apache-karaf-3.0.5/system/)
	shaded.org.eclipse.aether.resolution.ArtifactResolutionException: Could not find artifact org.apache.servicemix.bundles.kafka-clients:org.apache.servicemix.bundles.kafka-clients:jar:0.8.2.2_1 in system.repository (file:/opt/onos/apache-karaf-3.0.5/system/)
	        at shaded.org.eclipse.aether.internal.impl.DefaultArtifactResolver.resolve(DefaultArtifactResolver.java:444)
	        at shaded.org.eclipse.aether.internal.impl.DefaultArtifactResolver.resolveArtifacts(DefaultArtifactResolver.java:246)
	        at shaded.org.eclipse.aether.internal.impl.DefaultArtifactResolver.resolveArtifact(DefaultArtifactResolver.java:223)
	        at shaded.org.eclipse.aether.internal.impl.DefaultRepositorySystem.resolveArtifact(DefaultRepositorySystem.java:294)
	        at org.ops4j.pax.url.mvn.internal.AetherBasedResolver.resolve(AetherBasedResolver.java:573)
	        at org.ops4j.pax.url.mvn.internal.AetherBasedResolver.resolve(AetherBasedResolver.java:528)
	        at org.ops4j.pax.url.mvn.internal.AetherBasedResolver.resolve(AetherBasedResolver.java:506)
	        at org.ops4j.pax.url.mvn.internal.AetherBasedResolver.resolve(AetherBasedResolver.java:481)
	        at org.ops4j.pax.url.mvn.internal.Connection.getInputStream(Connection.java:123)
	        at java.net.URL.openStream(URL.java:1038)[:1.8.0_45]
	        at org.apache.karaf.features.internal.BundleManager.getInputStreamForBundle(BundleManager.java:230)
	        at org.apache.karaf.features.internal.BundleManager.doInstallBundleIfNeeded(BundleManager.java:96)
	        at org.apache.karaf.features.internal.BundleManager.installBundleIfNeeded(BundleManager.java:90)
	        at org.apache.karaf.features.internal.FeaturesServiceImpl.doInstallFeature(FeaturesServiceImpl.java:581)
	        at org.apache.karaf.features.internal.FeaturesServiceImpl.installFeatures(FeaturesServiceImpl.java:436)
	        at org.apache.karaf.features.internal.FeaturesServiceImpl.installFeature(FeaturesServiceImpl.java:417)
	        at org.apache.karaf.features.internal.FeaturesServiceImpl.installFeature(FeaturesServiceImpl.java:392)
	        at org.apache.karaf.features.internal.FeaturesServiceImpl.installFeature(FeaturesServiceImpl.java:373)
	        at org.apache.karaf.features.internal.FeaturesServiceImpl.installFeature(FeaturesServiceImpl.java:351)
	        at Proxy736afdfc_4519_4146_921a_2f3fe1ac745c.installFeature(Unknown Source)
	        at org.onosproject.app.impl.ApplicationManager.installAppFeatures(ApplicationManager.java:253)
	        at org.onosproject.app.impl.ApplicationManager.access$100(ApplicationManager.java:56)
	        at org.onosproject.app.impl.ApplicationManager$InternalStoreDelegate.notify(ApplicationManager.java:184)
	        at org.onosproject.app.impl.ApplicationManager$InternalStoreDelegate.notify(ApplicationManager.java:177)
	        at org.onosproject.store.app.GossipApplicationStore$InternalAppStatesListener.event(GossipApplicationStore.java:398)
	        at org.onosproject.store.ecmap.EventuallyConsistentMapImpl.lambda$notifyListeners$9(EventuallyConsistentMapImpl.java:512)
	        at org.onosproject.store.ecmap.EventuallyConsistentMapImpl$$Lambda$214/410503199.accept(Unknown Source)
	        at java.util.concurrent.CopyOnWriteArrayList.forEach(CopyOnWriteArrayList.java:890)[:1.8.0_45]
	        at java.util.concurrent.CopyOnWriteArraySet.forEach(CopyOnWriteArraySet.java:404)[:1.8.0_45]
	        at org.onosproject.store.ecmap.EventuallyConsistentMapImpl.notifyListeners(EventuallyConsistentMapImpl.java:512)
	        at org.onosproject.store.ecmap.EventuallyConsistentMapImpl.put(EventuallyConsistentMapImpl.java:323)
	        at org.onosproject.store.app.GossipApplicationStore.activate(GossipApplicationStore.java:316)
	        at org.onosproject.store.app.GossipApplicationStore.activate(GossipApplicationStore.java:305)
	        at org.onosproject.store.app.GossipApplicationStore.activate(GossipApplicationStore.java:300)
	        at org.onosproject.app.impl.ApplicationManager.activate(ApplicationManager.java:161)
	        at org.onosproject.rest.resources.ApplicationsWebResource.installApp(ApplicationsWebResource.java:90)[135:org.onosproject.onos-rest:1.4.0]
	        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)[:1.8.0_45]
	        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)[:1.8.0_45]
	        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)[:1.8.0_45]
	        at java.lang.reflect.Method.invoke(Method.java:497)[:1.8.0_45]
	        at com.sun.jersey.spi.container.JavaMethodInvokerFactory$1.invoke(JavaMethodInvokerFactory.java:60)[129:com.sun.jersey.jersey-server:1.19.0]
	        at com.sun.jersey.server.impl.model.method.dispatch.AbstractResourceMethodDispatchProvider$ResponseOutInvoker._dispatch(AbstractResourceMethodDispatchProvider.java:205)[129:com.sun.jersey.jersey-server:1.19.0]
	        at com.sun.jersey.server.impl.model.method.dispatch.ResourceJavaMethodDispatcher.dispatch(ResourceJavaMethodDispatcher.java:75)[129:com.sun.jersey.jersey-server:1.19.0]
	        at com.sun.jersey.server.impl.uri.rules.HttpMethodRule.accept(HttpMethodRule.java:302)[129:com.sun.jersey.jersey-server:1.19.0]
	        at com.sun.jersey.server.impl.uri.rules.ResourceClassRule.accept(ResourceClassRule.java:108)[129:com.sun.jersey.jersey-server:1.19.0]
	        at com.sun.jersey.server.impl.uri.rules.RightHandPathRule.accept(RightHandPathRule.java:147)[129:com.sun.jersey.jersey-server:1.19.0]
	        at com.sun.jersey.server.impl.uri.rules.RootResourceClassesRule.accept(RootResourceClassesRule.java:84)[129:com.sun.jersey.jersey-server:1.19.0]
	        at com.sun.jersey.server.impl.application.WebApplicationImpl._handleRequest(WebApplicationImpl.java:1542)[129:com.sun.jersey.jersey-server:1.19.0]
	        at com.sun.jersey.server.impl.application.WebApplicationImpl._handleRequest(WebApplicationImpl.java:1473)[129:com.sun.jersey.jersey-server:1.19.0]
	        at com.sun.jersey.server.impl.application.WebApplicationImpl.handleRequest(WebApplicationImpl.java:1419)[129:com.sun.jersey.jersey-server:1.19.0]
	        at com.sun.jersey.server.impl.application.WebApplicationImpl.handleRequest(WebApplicationImpl.java:1409)[129:com.sun.jersey.jersey-server:1.19.0]
	        at com.sun.jersey.spi.container.servlet.WebComponent.service(WebComponent.java:409)[130:com.sun.jersey.servlet:1.19.0]
	        at com.sun.jersey.spi.container.servlet.ServletContainer.service(ServletContainer.java:558)[130:com.sun.jersey.servlet:1.19.0]
	        at com.sun.jersey.spi.container.servlet.ServletContainer.service(ServletContainer.java:733)[130:com.sun.jersey.servlet:1.19.0]
	        at javax.servlet.http.HttpServlet.service(HttpServlet.java:668)[97:org.apache.geronimo.specs.geronimo-servlet_3.0_spec:1.0.0]
	        at org.eclipse.jetty.servlet.ServletHolder.handle(ServletHolder.java:684)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.servlet.ServletHandler.doHandle(ServletHandler.java:503)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.ops4j.pax.web.service.jetty.internal.HttpServiceServletHandler.doHandle(HttpServiceServletHandler.java:69)[111:org.ops4j.pax.web.pax-web-jetty:3.2.6]
	        at org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:137)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.security.SecurityHandler.handle(SecurityHandler.java:522)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.server.session.SessionHandler.doHandle(SessionHandler.java:231)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.server.handler.ContextHandler.doHandle(ContextHandler.java:1086)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.ops4j.pax.web.service.jetty.internal.HttpServiceContext.doHandle(HttpServiceContext.java:240)[111:org.ops4j.pax.web.pax-web-jetty:3.2.6]
	        at org.eclipse.jetty.servlet.ServletHandler.doScope(ServletHandler.java:429)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.server.session.SessionHandler.doScope(SessionHandler.java:193)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.server.handler.ContextHandler.doScope(ContextHandler.java:1020)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:135)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.ops4j.pax.web.service.jetty.internal.JettyServerHandlerCollection.handle(JettyServerHandlerCollection.java:75)[111:org.ops4j.pax.web.pax-web-jetty:3.2.6]
	        at org.eclipse.jetty.server.handler.HandlerWrapper.handle(HandlerWrapper.java:116)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.server.Server.handle(Server.java:370)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.server.AbstractHttpConnection.handleRequest(AbstractHttpConnection.java:494)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.server.AbstractHttpConnection.headerComplete(AbstractHttpConnection.java:971)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.server.AbstractHttpConnection$RequestHandler.headerComplete(AbstractHttpConnection.java:1033)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.http.HttpParser.parseNext(HttpParser.java:651)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.http.HttpParser.parseAvailable(HttpParser.java:235)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.server.AsyncHttpConnection.handle(AsyncHttpConnection.java:82)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.io.nio.SelectChannelEndPoint.handle(SelectChannelEndPoint.java:696)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.io.nio.SelectChannelEndPoint$1.run(SelectChannelEndPoint.java:53)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.util.thread.QueuedThreadPool.runJob(QueuedThreadPool.java:608)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.util.thread.QueuedThreadPool$3.run(QueuedThreadPool.java:543)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at java.lang.Thread.run(Thread.java:745)[:1.8.0_45]
	Caused by: shaded.org.eclipse.aether.transfer.ArtifactNotFoundException: Could not find artifact org.apache.servicemix.bundles.kafka-clients:org.apache.servicemix.bundles.kafka-clients:jar:0.8.2.2_1 in system.repository (file:/opt/onos/apache-karaf-3.0.5/system/)
	        at shaded.org.eclipse.aether.connector.basic.ArtifactTransportListener.transferFailed(ArtifactTransportListener.java:39)
	        at shaded.org.eclipse.aether.connector.basic.BasicRepositoryConnector$TaskRunner.run(BasicRepositoryConnector.java:355)
	        at shaded.org.eclipse.aether.util.concurrency.RunnableErrorForwarder$1.run(RunnableErrorForwarder.java:67)
	        at shaded.org.eclipse.aether.connector.basic.BasicRepositoryConnector$DirectExecutor.execute(BasicRepositoryConnector.java:581)
	        at shaded.org.eclipse.aether.connector.basic.BasicRepositoryConnector.get(BasicRepositoryConnector.java:249)
	        at shaded.org.eclipse.aether.internal.impl.DefaultArtifactResolver.performDownloads(DefaultArtifactResolver.java:520)
	        at shaded.org.eclipse.aether.internal.impl.DefaultArtifactResolver.resolve(DefaultArtifactResolver.java:421)
	        ... 80 more
	2016-03-07 11:15:33,802 | WARN  | qtp1055756261-61 | ApplicationManager               | 83 - org.onosproject.onos-core-net - 1.4.0 | Unable to perform operation on application com.foo.app
	java.lang.IllegalStateException: Can't install feature bar/0.0.0: 	
	Error resolving artifact org.apache.servicemix.bundles.kafka-clients:org.apache.servicemix.bundles.kafka-clients:jar:0.8.2.2_1: Could not find artifact org.apache.servicemix.bundles.kafka-clients:org.apache.servicemix.bundles.kafka-clients:jar:0.8.2.2_1 in system.repository (file:/opt/onos/apache-karaf-3.0.5/system/)
	        at org.apache.karaf.features.internal.FeaturesServiceImpl.installFeature(FeaturesServiceImpl.java:405)
	        at org.apache.karaf.features.internal.FeaturesServiceImpl.installFeature(FeaturesServiceImpl.java:373)
	        at org.apache.karaf.features.internal.FeaturesServiceImpl.installFeature(FeaturesServiceImpl.java:351)
	        at Proxy736afdfc_4519_4146_921a_2f3fe1ac745c.installFeature(Unknown Source)
	        at org.onosproject.app.impl.ApplicationManager.installAppFeatures(ApplicationManager.java:253)
	        at org.onosproject.app.impl.ApplicationManager.access$100(ApplicationManager.java:56)
	        at org.onosproject.app.impl.ApplicationManager$InternalStoreDelegate.notify(ApplicationManager.java:184)
	        at org.onosproject.app.impl.ApplicationManager$InternalStoreDelegate.notify(ApplicationManager.java:177)
	        at org.onosproject.store.app.GossipApplicationStore$InternalAppStatesListener.event(GossipApplicationStore.java:398)
	        at org.onosproject.store.ecmap.EventuallyConsistentMapImpl.lambda$notifyListeners$9(EventuallyConsistentMapImpl.java:512)
	        at org.onosproject.store.ecmap.EventuallyConsistentMapImpl$$Lambda$214/410503199.accept(Unknown Source)
	        at java.util.concurrent.CopyOnWriteArrayList.forEach(CopyOnWriteArrayList.java:890)[:1.8.0_45]
	        at java.util.concurrent.CopyOnWriteArraySet.forEach(CopyOnWriteArraySet.java:404)[:1.8.0_45]
	        at org.onosproject.store.ecmap.EventuallyConsistentMapImpl.notifyListeners(EventuallyConsistentMapImpl.java:512)
	        at org.onosproject.store.ecmap.EventuallyConsistentMapImpl.put(EventuallyConsistentMapImpl.java:323)
	        at org.onosproject.store.app.GossipApplicationStore.activate(GossipApplicationStore.java:316)
	        at org.onosproject.store.app.GossipApplicationStore.activate(GossipApplicationStore.java:305)
	        at org.onosproject.store.app.GossipApplicationStore.activate(GossipApplicationStore.java:300)
	        at org.onosproject.app.impl.ApplicationManager.activate(ApplicationManager.java:161)
	        at org.onosproject.rest.resources.ApplicationsWebResource.installApp(ApplicationsWebResource.java:90)[135:org.onosproject.onos-rest:1.4.0]
	        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)[:1.8.0_45]
	        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)[:1.8.0_45]
	        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)[:1.8.0_45]
	        at java.lang.reflect.Method.invoke(Method.java:497)[:1.8.0_45]
	        at com.sun.jersey.spi.container.JavaMethodInvokerFactory$1.invoke(JavaMethodInvokerFactory.java:60)[129:com.sun.jersey.jersey-server:1.19.0]
	        at com.sun.jersey.server.impl.model.method.dispatch.AbstractResourceMethodDispatchProvider$ResponseOutInvoker._dispatch(AbstractResourceMethodDispatchProvider.java:205)[129:com.sun.jersey.jersey-server:1.19.0]
	        at com.sun.jersey.server.impl.model.method.dispatch.ResourceJavaMethodDispatcher.dispatch(ResourceJavaMethodDispatcher.java:75)[129:com.sun.jersey.jersey-server:1.19.0]
	        at com.sun.jersey.server.impl.uri.rules.HttpMethodRule.accept(HttpMethodRule.java:302)[129:com.sun.jersey.jersey-server:1.19.0]
	        at com.sun.jersey.server.impl.uri.rules.ResourceClassRule.accept(ResourceClassRule.java:108)[129:com.sun.jersey.jersey-server:1.19.0]
	        at com.sun.jersey.server.impl.uri.rules.RightHandPathRule.accept(RightHandPathRule.java:147)[129:com.sun.jersey.jersey-server:1.19.0]
	        at com.sun.jersey.server.impl.uri.rules.RootResourceClassesRule.accept(RootResourceClassesRule.java:84)[129:com.sun.jersey.jersey-server:1.19.0]
	        at com.sun.jersey.server.impl.application.WebApplicationImpl._handleRequest(WebApplicationImpl.java:1542)[129:com.sun.jersey.jersey-server:1.19.0]
	        at com.sun.jersey.server.impl.application.WebApplicationImpl._handleRequest(WebApplicationImpl.java:1473)[129:com.sun.jersey.jersey-server:1.19.0]
	        at com.sun.jersey.server.impl.application.WebApplicationImpl.handleRequest(WebApplicationImpl.java:1419)[129:com.sun.jersey.jersey-server:1.19.0]
	        at com.sun.jersey.server.impl.application.WebApplicationImpl.handleRequest(WebApplicationImpl.java:1409)[129:com.sun.jersey.jersey-server:1.19.0]
	        at com.sun.jersey.spi.container.servlet.WebComponent.service(WebComponent.java:409)[130:com.sun.jersey.servlet:1.19.0]
	        at com.sun.jersey.spi.container.servlet.ServletContainer.service(ServletContainer.java:558)[130:com.sun.jersey.servlet:1.19.0]
	        at com.sun.jersey.spi.container.servlet.ServletContainer.service(ServletContainer.java:733)[130:com.sun.jersey.servlet:1.19.0]
	        at javax.servlet.http.HttpServlet.service(HttpServlet.java:668)[97:org.apache.geronimo.specs.geronimo-servlet_3.0_spec:1.0.0]
	        at org.eclipse.jetty.servlet.ServletHolder.handle(ServletHolder.java:684)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.servlet.ServletHandler.doHandle(ServletHandler.java:503)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.ops4j.pax.web.service.jetty.internal.HttpServiceServletHandler.doHandle(HttpServiceServletHandler.java:69)[111:org.ops4j.pax.web.pax-web-jetty:3.2.6]
	        at org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:137)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.security.SecurityHandler.handle(SecurityHandler.java:522)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.server.session.SessionHandler.doHandle(SessionHandler.java:231)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.server.handler.ContextHandler.doHandle(ContextHandler.java:1086)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.ops4j.pax.web.service.jetty.internal.HttpServiceContext.doHandle(HttpServiceContext.java:240)[111:org.ops4j.pax.web.pax-web-jetty:3.2.6]
	        at org.eclipse.jetty.servlet.ServletHandler.doScope(ServletHandler.java:429)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.server.session.SessionHandler.doScope(SessionHandler.java:193)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.server.handler.ContextHandler.doScope(ContextHandler.java:1020)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:135)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.ops4j.pax.web.service.jetty.internal.JettyServerHandlerCollection.handle(JettyServerHandlerCollection.java:75)[111:org.ops4j.pax.web.pax-web-jetty:3.2.6]
	        at org.eclipse.jetty.server.handler.HandlerWrapper.handle(HandlerWrapper.java:116)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.server.Server.handle(Server.java:370)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.server.AbstractHttpConnection.handleRequest(AbstractHttpConnection.java:494)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.server.AbstractHttpConnection.headerComplete(AbstractHttpConnection.java:971)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.server.AbstractHttpConnection$RequestHandler.headerComplete(AbstractHttpConnection.java:1033)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.http.HttpParser.parseNext(HttpParser.java:651)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.http.HttpParser.parseAvailable(HttpParser.java:235)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.server.AsyncHttpConnection.handle(AsyncHttpConnection.java:82)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.io.nio.SelectChannelEndPoint.handle(SelectChannelEndPoint.java:696)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.io.nio.SelectChannelEndPoint$1.run(SelectChannelEndPoint.java:53)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.util.thread.QueuedThreadPool.runJob(QueuedThreadPool.java:608)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.util.thread.QueuedThreadPool$3.run(QueuedThreadPool.java:543)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at java.lang.Thread.run(Thread.java:745)[:1.8.0_45]

### Application is listed as activated in `apps -s`
onos> apps -s
	*   1 org.onosproject.openflow-base    1.4.0    OpenFlow protocol southbound providers
	*   2 org.onosproject.hostprovider     1.4.0    ONOS host location provider
	*   3 org.onosproject.lldpprovider     1.4.0    ONOS LLDP link provider
	*   4 org.onosproject.openflow         1.4.0    OpenFlow southbound meta application
	    5 org.onosproject.election         1.4.0    Master election test application
	    6 org.onosproject.openstackswitching 1.4.0    SONA Openstack Switching  applications
	    7 org.onosproject.bgp              1.4.0    BGP protocol southbound providers
	    8 org.onosproject.incubator.rpc    1.4.0    ONOS inter-cluster RPC service
	    9 org.onosproject.cip              1.4.0    Cluster IP alias
	   10 org.onosproject.aaa              1.4.0    ONOS authentication application
	   11 org.onosproject.netconf          1.4.0    NetConf protocol southbound providers
	   12 org.onosproject.reactive.routing 1.4.0    SDN-IP reactive routing application
	   13 org.onosproject.flowanalyzer     1.4.0    Simple flow space analyzer
	   14 org.onosproject.mobility         1.4.0    Host mobility application
	   15 org.onosproject.messagingperf    1.4.0    Messaging performance test application
	   16 org.onosproject.vtn              1.4.0    ONOS framework applications
	   17 org.onosproject.netcfghostprovider 1.4.0    Host provider that uses network config service to discover hosts.
	   18 org.onosproject.drivermatrix     1.4.0    Driver behaviour support matric
	   19 org.onosproject.cordfabric       1.4.0    Simple fabric application for CORD
	*  20 org.onosproject.fwd              1.4.0    Reactive forwarding application using flow subsystem
	   21 org.onosproject.igmp             1.4.0    Internet Group Message Protocol
	   22 org.onosproject.pathpainter      1.4.0    Path visualization application
	*  23 org.onosproject.drivers          1.4.0    Builtin device drivers
	   24 org.onosproject.distributedprimitives 1.4.0    ONOS app to test distributed primitives
	   25 org.onosproject.olt              1.4.0    OLT application
	   26 org.onosproject.intentperf       1.4.0    Intent performance test application
	   27 org.onosproject.ovsdb            1.4.0    ONOS information providers and control/management protocol adapter
	   28 org.onosproject.cordvtn          1.4.0    Virtual tenant network service for CORD
	   29 org.onosproject.incubator.rpc.grpc 1.4.0    ONOS inter-cluster RPC based on gRPC
	*  30 org.onosproject.proxyarp         1.4.0    Proxy ARP/NDP application
	   31 org.onosproject.optical          1.4.0    Packet/Optical use-case application
	   32 org.onosproject.mfwd             1.4.0    Multicast forwarding application
	   33 org.onosproject.dhcp             1.4.0    DHCP Server application
	   34 org.onosproject.sdnip            1.4.0    SDN-IP peering application
	   35 org.onosproject.xosintegration   1.4.0    ONOS XOS integration application
	   36 org.onosproject.metrics          1.4.0    Performance metrics collection
	   37 org.onosproject.mlb              1.4.0    Balances mastership among nodes
	   38 org.onosproject.faultmanagement  1.4.0    ONOS framework applications
	   39 org.onosproject.pim              1.4.0    Protocol Independent Multicast Emulation
	   40 org.onosproject.bgprouter        1.4.0    BGP router application
	   41 org.onosproject.pcep             1.4.0    PCEP protocol southbound providers
	   42 org.onosproject.demo             1.4.0    Flow throughput test application
	   43 org.onosproject.virtualbng       1.4.0    A virtual Broadband Network Gateway(BNG) application
	   44 org.onosproject.segmentrouting   1.4.0    Segment routing application
	   45 org.onosproject.acl              1.4.0    ONOS ACL application
	   46 org.onosproject.null             1.4.0    Null southbound providers
	*  53 com.foo.app                      1.0.SNAPSHOT ONOS OSGi bundle archetype


onos> app uninstall com.foo.app 
onos> log:clear
onos> shutdown -c -cc -h

</code></pre>

## Third Try
In this try, I created the `app.xml` file following the previous suggestions like this:

```
<app name="com.foo.app"
     origin="Foo, Inc."
     version="${project.version}"
     featuresRepo="mvn:${project.groupId}/${project.artifactId}/${project.version}/xml/features"
     features="${project.artifactId}">
    <description>${project.description}</description>
    <artifact>mvn:${project.groupId}/${project.artifactId}/${project.version}</artifact>
    <bundle>mvn:org.apache.servicemix.bundles.kafka-clients/org.apache.servicemix.bundles.kafka-clients/0.8.2.2_1</bundle>
</app>
```
The result was the same as of second try.


Browse the code at the end of this stage here: [3rd try. Features.xml and app.xml. Application fails to get activated.](https://github.com/kyrsideris/failing-onos-app-puzzle/tree/c17df9d9da148238ef2ed2688ce7ff53da283f3a)

<pre><code>
$ mvn clean install

	[INFO] Scanning for projects...
	[INFO]                                                                         
	[INFO] ------------------------------------------------------------------------
	[INFO] Building bar 1.0-SNAPSHOT
	[INFO] ------------------------------------------------------------------------
	[INFO] 
	[INFO] --- maven-clean-plugin:2.5:clean (default-clean) @ bar ---
	[INFO] 
	[INFO] --- onos-maven-plugin:1.5:swagger (swagger) @ bar ---
	[INFO] 
	[INFO] --- onos-maven-plugin:1.5:cfg (cfg) @ bar ---
	[INFO] Generating ONOS component configuration catalogues...
	[INFO] 
	[INFO] --- maven-resources-plugin:2.7:resources (default-resources) @ bar ---
	[WARNING] Using platform encoding (UTF-8 actually) to copy filtered resources, i.e. build is platform dependent!
	[INFO] skip non existing resourceDirectory /workspace/bar/src/main/resources
	[INFO] 
	[INFO] --- maven-compiler-plugin:2.5.1:compile (default-compile) @ bar ---
	[WARNING] File encoding has not been set, using platform encoding UTF-8, i.e. build is platform dependent!
	[INFO] Compiling 1 source file to /workspace/bar/target/classes
	[INFO] 
	[INFO] --- maven-scr-plugin:1.20.0:scr (generate-scr-srcdescriptor) @ bar ---
	[INFO] Writing 1 Service Component Descriptors to /workspace/bar/target/classes/OSGI-INF/com.foo.bar.AppComponent.xml
	[INFO] 
	[INFO] --- maven-resources-plugin:2.7:testResources (default-testResources) @ bar ---
	[WARNING] Using platform encoding (UTF-8 actually) to copy filtered resources, i.e. build is platform dependent!
	[INFO] skip non existing resourceDirectory /workspace/bar/src/test/resources
	[INFO] 
	[INFO] --- maven-compiler-plugin:2.5.1:testCompile (default-testCompile) @ bar ---
	[WARNING] File encoding has not been set, using platform encoding UTF-8, i.e. build is platform dependent!
	[INFO] Compiling 5 source files to /workspace/bar/target/test-classes
	[INFO] 
	[INFO] --- maven-surefire-plugin:2.19.1:test (default-test) @ bar ---
	
	-------------------------------------------------------
	 T E S T S
	-------------------------------------------------------
	Running com.foo.bar.AppComponentTest
	SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
	SLF4J: Defaulting to no-operation (NOP) logger implementation
	SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
	Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.202 sec - in com.foo.bar.AppComponentTest
	
	Results :
	
	Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
	
	[INFO] 
	[INFO] --- maven-bundle-plugin:2.5.3:bundle (default-bundle) @ bar ---
	[INFO] 
	[INFO] --- onos-maven-plugin:1.5:app (app) @ bar ---
	[INFO] Building ONOS application package for com.foo.app (v1.0-SNAPSHOT)
	[INFO] 
	[INFO] --- maven-install-plugin:2.5.2:install (default-install) @ bar ---
	[INFO] Installing /workspace/bar/target/bar-1.0-SNAPSHOT.jar to /home/kyriakos/.m2/repository/com/foo/bar/1.0-SNAPSHOT/bar-1.0-SNAPSHOT.jar
	[INFO] Installing /workspace/bar/pom.xml to /home/kyriakos/.m2/repository/com/foo/bar/1.0-SNAPSHOT/bar-1.0-SNAPSHOT.pom
	[INFO] Installing /workspace/bar/target/bar-1.0-SNAPSHOT.oar to /home/kyriakos/.m2/repository/com/foo/bar/1.0-SNAPSHOT/bar-1.0-SNAPSHOT.oar
	[INFO] 
	[INFO] --- maven-bundle-plugin:2.5.3:install (default-install) @ bar ---
	[INFO] Installing com/foo/bar/1.0-SNAPSHOT/bar-1.0-SNAPSHOT.jar
	[INFO] Writing OBR metadata
	[INFO] ------------------------------------------------------------------------
	[INFO] BUILD SUCCESS
	[INFO] ------------------------------------------------------------------------
	[INFO] Total time: 4.727 s
	[INFO] Finished at: 2016-03-07T11:31:01+00:00
	[INFO] Final Memory: 26M/321M
	[INFO] ------------------------------------------------------------------------


onos> log:clear


$ onos-app $OCI install! target/bar-1.0-SNAPSHOT.oar 

	{"name":"com.foo.app","id":53,"version":"1.0.SNAPSHOT","description":"ONOS OSGi bundle archetype","origin":"Foo, Inc.","permissions":"[]","featuresRepo":"mvn:com.foo/bar/1.0-SNAPSHOT/xml/features","features":"[bar]","requiredApps":"[]","state":"ACTIVE"}

onos> log:display
		2016-03-07 11:35:14,667 | INFO  | qtp602891664-65  | ApplicationManager               | 83 - org.onosproject.onos-core-net - 1.4.0 | Application com.foo.app has been installed
		2016-03-07 11:35:14,676 | INFO  | qtp602891664-65  | FeaturesServiceImpl              | 20 - org.apache.karaf.features.core - 3.0.5 | Installing feature bar 1.0-SNAPSHOT
		2016-03-07 11:35:15,675 | WARN  | qtp602891664-65  | AetherBasedResolver              | 3 - org.ops4j.pax.logging.pax-logging-api - 1.8.4 | Error resolving artifactorg.apache.servicemix.bundles.kafka-clients:org.apache.servicemix.bundles.kafka-clients:jar:0.8.2.2_1:Could not find artifact org.apache.servicemix.bundles.kafka-clients:org.apache.servicemix.bundles.kafka-clients:jar:0.8.2.2_1 in system.repository (file:/opt/onos/apache-karaf-3.0.5/system/)
		shaded.org.eclipse.aether.resolution.ArtifactResolutionException: Could not find artifact org.apache.servicemix.bundles.kafka-clients:org.apache.servicemix.bundles.kafka-clients:jar:0.8.2.2_1 in system.repository (file:/opt/onos/apache-karaf-3.0.5/system/)
		        at shaded.org.eclipse.aether.internal.impl.DefaultArtifactResolver.resolve(DefaultArtifactResolver.java:444)
		        at shaded.org.eclipse.aether.internal.impl.DefaultArtifactResolver.resolveArtifacts(DefaultArtifactResolver.java:246)
		        at shaded.org.eclipse.aether.internal.impl.DefaultArtifactResolver.resolveArtifact(DefaultArtifactResolver.java:223)
		        at shaded.org.eclipse.aether.internal.impl.DefaultRepositorySystem.resolveArtifact(DefaultRepositorySystem.java:294)
		        at org.ops4j.pax.url.mvn.internal.AetherBasedResolver.resolve(AetherBasedResolver.java:573)
		        at org.ops4j.pax.url.mvn.internal.AetherBasedResolver.resolve(AetherBasedResolver.java:528)
		        at org.ops4j.pax.url.mvn.internal.AetherBasedResolver.resolve(AetherBasedResolver.java:506)
		        at org.ops4j.pax.url.mvn.internal.AetherBasedResolver.resolve(AetherBasedResolver.java:481)
		        at org.ops4j.pax.url.mvn.internal.Connection.getInputStream(Connection.java:123)
		        at java.net.URL.openStream(URL.java:1038)[:1.8.0_45]
		        at org.apache.karaf.features.internal.BundleManager.getInputStreamForBundle(BundleManager.java:230)
		        at org.apache.karaf.features.internal.BundleManager.doInstallBundleIfNeeded(BundleManager.java:96)
		        at org.apache.karaf.features.internal.BundleManager.installBundleIfNeeded(BundleManager.java:90)
		        at org.apache.karaf.features.internal.FeaturesServiceImpl.doInstallFeature(FeaturesServiceImpl.java:581)
		        at org.apache.karaf.features.internal.FeaturesServiceImpl.installFeatures(FeaturesServiceImpl.java:436)
		        at org.apache.karaf.features.internal.FeaturesServiceImpl.installFeature(FeaturesServiceImpl.java:417)
		        at org.apache.karaf.features.internal.FeaturesServiceImpl.installFeature(FeaturesServiceImpl.java:392)
		        at org.apache.karaf.features.internal.FeaturesServiceImpl.installFeature(FeaturesServiceImpl.java:373)
		        at org.apache.karaf.features.internal.FeaturesServiceImpl.installFeature(FeaturesServiceImpl.java:351)
		        at Proxy8410c85e_df12_49d9_9809_57f22abc3554.installFeature(Unknown Source)
		        at org.onosproject.app.impl.ApplicationManager.installAppFeatures(ApplicationManager.java:253)
		        at org.onosproject.app.impl.ApplicationManager.access$100(ApplicationManager.java:56)
		        at org.onosproject.app.impl.ApplicationManager$InternalStoreDelegate.notify(ApplicationManager.java:184)
		        at org.onosproject.app.impl.ApplicationManager$InternalStoreDelegate.notify(ApplicationManager.java:177)
		        at org.onosproject.store.app.GossipApplicationStore$InternalAppStatesListener.event(GossipApplicationStore.java:398)
		        at org.onosproject.store.ecmap.EventuallyConsistentMapImpl.lambda$notifyListeners$9(EventuallyConsistentMapImpl.java:512)
		        at org.onosproject.store.ecmap.EventuallyConsistentMapImpl$$Lambda$213/1002092238.accept(Unknown Source)
		        at java.util.concurrent.CopyOnWriteArrayList.forEach(CopyOnWriteArrayList.java:890)[:1.8.0_45]
		        at java.util.concurrent.CopyOnWriteArraySet.forEach(CopyOnWriteArraySet.java:404)[:1.8.0_45]
		        at org.onosproject.store.ecmap.EventuallyConsistentMapImpl.notifyListeners(EventuallyConsistentMapImpl.java:512)
		        at org.onosproject.store.ecmap.EventuallyConsistentMapImpl.put(EventuallyConsistentMapImpl.java:323)
		        at org.onosproject.store.app.GossipApplicationStore.activate(GossipApplicationStore.java:316)
		        at org.onosproject.store.app.GossipApplicationStore.activate(GossipApplicationStore.java:305)
		        at org.onosproject.store.app.GossipApplicationStore.activate(GossipApplicationStore.java:300)
		        at org.onosproject.app.impl.ApplicationManager.activate(ApplicationManager.java:161)
		        at org.onosproject.rest.resources.ApplicationsWebResource.installApp(ApplicationsWebResource.java:90)[135:org.onosproject.onos-rest:1.4.0]
		        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)[:1.8.0_45]
		        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)[:1.8.0_45]
		        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)[:1.8.0_45]
		        at java.lang.reflect.Method.invoke(Method.java:497)[:1.8.0_45]
		        at com.sun.jersey.spi.container.JavaMethodInvokerFactory$1.invoke(JavaMethodInvokerFactory.java:60)[129:com.sun.jersey.jersey-server:1.19.0]
		        at com.sun.jersey.server.impl.model.method.dispatch.AbstractResourceMethodDispatchProvider$ResponseOutInvoker._dispatch(AbstractResourceMethodDispatchProvider.java:205)[129:com.sun.jersey.jersey-server:1.19.0]
		        at com.sun.jersey.server.impl.model.method.dispatch.ResourceJavaMethodDispatcher.dispatch(ResourceJavaMethodDispatcher.java:75)[129:com.sun.jersey.jersey-server:1.19.0]
		        at com.sun.jersey.server.impl.uri.rules.HttpMethodRule.accept(HttpMethodRule.java:302)[129:com.sun.jersey.jersey-server:1.19.0]
		        at com.sun.jersey.server.impl.uri.rules.ResourceClassRule.accept(ResourceClassRule.java:108)[129:com.sun.jersey.jersey-server:1.19.0]
		        at com.sun.jersey.server.impl.uri.rules.RightHandPathRule.accept(RightHandPathRule.java:147)[129:com.sun.jersey.jersey-server:1.19.0]
		        at com.sun.jersey.server.impl.uri.rules.RootResourceClassesRule.accept(RootResourceClassesRule.java:84)[129:com.sun.jersey.jersey-server:1.19.0]
		        at com.sun.jersey.server.impl.application.WebApplicationImpl._handleRequest(WebApplicationImpl.java:1542)[129:com.sun.jersey.jersey-server:1.19.0]
		        at com.sun.jersey.server.impl.application.WebApplicationImpl._handleRequest(WebApplicationImpl.java:1473)[129:com.sun.jersey.jersey-server:1.19.0]
		        at com.sun.jersey.server.impl.application.WebApplicationImpl.handleRequest(WebApplicationImpl.java:1419)[129:com.sun.jersey.jersey-server:1.19.0]
		        at com.sun.jersey.server.impl.application.WebApplicationImpl.handleRequest(WebApplicationImpl.java:1409)[129:com.sun.jersey.jersey-server:1.19.0]
		        at com.sun.jersey.spi.container.servlet.WebComponent.service(WebComponent.java:409)[130:com.sun.jersey.servlet:1.19.0]
		        at com.sun.jersey.spi.container.servlet.ServletContainer.service(ServletContainer.java:558)[130:com.sun.jersey.servlet:1.19.0]
		        at com.sun.jersey.spi.container.servlet.ServletContainer.service(ServletContainer.java:733)[130:com.sun.jersey.servlet:1.19.0]
		        at javax.servlet.http.HttpServlet.service(HttpServlet.java:668)[97:org.apache.geronimo.specs.geronimo-servlet_3.0_spec:1.0.0]
		        at org.eclipse.jetty.servlet.ServletHolder.handle(ServletHolder.java:684)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
		        at org.eclipse.jetty.servlet.ServletHandler.doHandle(ServletHandler.java:503)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
		        at org.ops4j.pax.web.service.jetty.internal.HttpServiceServletHandler.doHandle(HttpServiceServletHandler.java:69)[111:org.ops4j.pax.web.pax-web-jetty:3.2.6]
		        at org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:137)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
		        at org.eclipse.jetty.security.SecurityHandler.handle(SecurityHandler.java:522)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
		        at org.eclipse.jetty.server.session.SessionHandler.doHandle(SessionHandler.java:231)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
		        at org.eclipse.jetty.server.handler.ContextHandler.doHandle(ContextHandler.java:1086)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
		        at org.ops4j.pax.web.service.jetty.internal.HttpServiceContext.doHandle(HttpServiceContext.java:240)[111:org.ops4j.pax.web.pax-web-jetty:3.2.6]
		        at org.eclipse.jetty.servlet.ServletHandler.doScope(ServletHandler.java:429)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
		        at org.eclipse.jetty.server.session.SessionHandler.doScope(SessionHandler.java:193)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
		        at org.eclipse.jetty.server.handler.ContextHandler.doScope(ContextHandler.java:1020)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
		        at org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:135)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
		        at org.ops4j.pax.web.service.jetty.internal.JettyServerHandlerCollection.handle(JettyServerHandlerCollection.java:75)[111:org.ops4j.pax.web.pax-web-jetty:3.2.6]
		        at org.eclipse.jetty.server.handler.HandlerWrapper.handle(HandlerWrapper.java:116)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
		        at org.eclipse.jetty.server.Server.handle(Server.java:370)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
		        at org.eclipse.jetty.server.AbstractHttpConnection.handleRequest(AbstractHttpConnection.java:494)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
		        at org.eclipse.jetty.server.AbstractHttpConnection.headerComplete(AbstractHttpConnection.java:971)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
		        at org.eclipse.jetty.server.AbstractHttpConnection$RequestHandler.headerComplete(AbstractHttpConnection.java:1033)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
		        at org.eclipse.jetty.http.HttpParser.parseNext(HttpParser.java:651)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
		        at org.eclipse.jetty.http.HttpParser.parseAvailable(HttpParser.java:235)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
		        at org.eclipse.jetty.server.AsyncHttpConnection.handle(AsyncHttpConnection.java:82)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
		        at org.eclipse.jetty.io.nio.SelectChannelEndPoint.handle(SelectChannelEndPoint.java:696)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
		        at org.eclipse.jetty.io.nio.SelectChannelEndPoint$1.run(SelectChannelEndPoint.java:53)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
		        at org.eclipse.jetty.util.thread.QueuedThreadPool.runJob(QueuedThreadPool.java:608)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
		        at org.eclipse.jetty.util.thread.QueuedThreadPool$3.run(QueuedThreadPool.java:543)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
		        at java.lang.Thread.run(Thread.java:745)[:1.8.0_45]
		Caused by: shaded.org.eclipse.aether.transfer.ArtifactNotFoundException: Could not find artifact org.apache.servicemix.bundles.kafka-clients:org.apache.servicemix.bundles.kafka-clients:jar:0.8.2.2_1 in system.repository (file:/opt/onos/apache-karaf-3.0.5/system/)
		        at shaded.org.eclipse.aether.connector.basic.ArtifactTransportListener.transferFailed(ArtifactTransportListener.java:39)
		        at shaded.org.eclipse.aether.connector.basic.BasicRepositoryConnector$TaskRunner.run(BasicRepositoryConnector.java:355)
		        at shaded.org.eclipse.aether.util.concurrency.RunnableErrorForwarder$1.run(RunnableErrorForwarder.java:67)
		        at shaded.org.eclipse.aether.connector.basic.BasicRepositoryConnector$DirectExecutor.execute(BasicRepositoryConnector.java:581)
		        at shaded.org.eclipse.aether.connector.basic.BasicRepositoryConnector.get(BasicRepositoryConnector.java:249)
		        at shaded.org.eclipse.aether.internal.impl.DefaultArtifactResolver.performDownloads(DefaultArtifactResolver.java:520)
		        at shaded.org.eclipse.aether.internal.impl.DefaultArtifactResolver.resolve(DefaultArtifactResolver.java:421)
		        ... 80 more
		2016-03-07 11:35:15,718 | WARN  | qtp602891664-65  | ApplicationManager               | 83 - org.onosproject.onos-core-net - 1.4.0 | Unable to perform operation on application com.foo.app
		java.lang.IllegalStateException: Can't install feature bar/0.0.0: 	
		Error resolving artifact org.apache.servicemix.bundles.kafka-clients:org.apache.servicemix.bundles.kafka-clients:jar:0.8.2.2_1: Could not find artifact org.apache.servicemix.bundles.kafka-clients:org.apache.servicemix.bundles.kafka-clients:jar:0.8.2.2_1 in system.repository (file:/opt/onos/apache-karaf-3.0.5/system/)
		        at org.apache.karaf.features.internal.FeaturesServiceImpl.installFeature(FeaturesServiceImpl.java:405)
		        at org.apache.karaf.features.internal.FeaturesServiceImpl.installFeature(FeaturesServiceImpl.java:373)
		        at org.apache.karaf.features.internal.FeaturesServiceImpl.installFeature(FeaturesServiceImpl.java:351)
		        at Proxy8410c85e_df12_49d9_9809_57f22abc3554.installFeature(Unknown Source)
		        at org.onosproject.app.impl.ApplicationManager.installAppFeatures(ApplicationManager.java:253)
		        at org.onosproject.app.impl.ApplicationManager.access$100(ApplicationManager.java:56)
		        at org.onosproject.app.impl.ApplicationManager$InternalStoreDelegate.notify(ApplicationManager.java:184)
		        at org.onosproject.app.impl.ApplicationManager$InternalStoreDelegate.notify(ApplicationManager.java:177)
		        at org.onosproject.store.app.GossipApplicationStore$InternalAppStatesListener.event(GossipApplicationStore.java:398)
		        at org.onosproject.store.ecmap.EventuallyConsistentMapImpl.lambda$notifyListeners$9(EventuallyConsistentMapImpl.java:512)
		        at org.onosproject.store.ecmap.EventuallyConsistentMapImpl$$Lambda$213/1002092238.accept(Unknown Source)
		        at java.util.concurrent.CopyOnWriteArrayList.forEach(CopyOnWriteArrayList.java:890)[:1.8.0_45]
		        at java.util.concurrent.CopyOnWriteArraySet.forEach(CopyOnWriteArraySet.java:404)[:1.8.0_45]
		        at org.onosproject.store.ecmap.EventuallyConsistentMapImpl.notifyListeners(EventuallyConsistentMapImpl.java:512)
		        at org.onosproject.store.ecmap.EventuallyConsistentMapImpl.put(EventuallyConsistentMapImpl.java:323)
		        at org.onosproject.store.app.GossipApplicationStore.activate(GossipApplicationStore.java:316)
		        at org.onosproject.store.app.GossipApplicationStore.activate(GossipApplicationStore.java:305)
		        at org.onosproject.store.app.GossipApplicationStore.activate(GossipApplicationStore.java:300)
		        at org.onosproject.app.impl.ApplicationManager.activate(ApplicationManager.java:161)
		        at org.onosproject.rest.resources.ApplicationsWebResource.installApp(ApplicationsWebResource.java:90)[135:org.onosproject.onos-rest:1.4.0]
		        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)[:1.8.0_45]
		        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)[:1.8.0_45]
		        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)[:1.8.0_45]
		        at java.lang.reflect.Method.invoke(Method.java:497)[:1.8.0_45]
		        at com.sun.jersey.spi.container.JavaMethodInvokerFactory$1.invoke(JavaMethodInvokerFactory.java:60)[129:com.sun.jersey.jersey-server:1.19.0]
		        at com.sun.jersey.server.impl.model.method.dispatch.AbstractResourceMethodDispatchProvider$ResponseOutInvoker._dispatch(AbstractResourceMethodDispatchProvider.java:205)[129:com.sun.jersey.jersey-server:1.19.0]
		        at com.sun.jersey.server.impl.model.method.dispatch.ResourceJavaMethodDispatcher.dispatch(ResourceJavaMethodDispatcher.java:75)[129:com.sun.jersey.jersey-server:1.19.0]
		        at com.sun.jersey.server.impl.uri.rules.HttpMethodRule.accept(HttpMethodRule.java:302)[129:com.sun.jersey.jersey-server:1.19.0]
		        at com.sun.jersey.server.impl.uri.rules.ResourceClassRule.accept(ResourceClassRule.java:108)[129:com.sun.jersey.jersey-server:1.19.0]
		        at com.sun.jersey.server.impl.uri.rules.RightHandPathRule.accept(RightHandPathRule.java:147)[129:com.sun.jersey.jersey-server:1.19.0]
		        at com.sun.jersey.server.impl.uri.rules.RootResourceClassesRule.accept(RootResourceClassesRule.java:84)[129:com.sun.jersey.jersey-server:1.19.0]
		        at com.sun.jersey.server.impl.application.WebApplicationImpl._handleRequest(WebApplicationImpl.java:1542)[129:com.sun.jersey.jersey-server:1.19.0]
		        at com.sun.jersey.server.impl.application.WebApplicationImpl._handleRequest(WebApplicationImpl.java:1473)[129:com.sun.jersey.jersey-server:1.19.0]
		        at com.sun.jersey.server.impl.application.WebApplicationImpl.handleRequest(WebApplicationImpl.java:1419)[129:com.sun.jersey.jersey-server:1.19.0]
		        at com.sun.jersey.server.impl.application.WebApplicationImpl.handleRequest(WebApplicationImpl.java:1409)[129:com.sun.jersey.jersey-server:1.19.0]
		        at com.sun.jersey.spi.container.servlet.WebComponent.service(WebComponent.java:409)[130:com.sun.jersey.servlet:1.19.0]
		        at com.sun.jersey.spi.container.servlet.ServletContainer.service(ServletContainer.java:558)[130:com.sun.jersey.servlet:1.19.0]
		        at com.sun.jersey.spi.container.servlet.ServletContainer.service(ServletContainer.java:733)[130:com.sun.jersey.servlet:1.19.0]
		        at javax.servlet.http.HttpServlet.service(HttpServlet.java:668)[97:org.apache.geronimo.specs.geronimo-servlet_3.0_spec:1.0.0]
		        at org.eclipse.jetty.servlet.ServletHolder.handle(ServletHolder.java:684)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
		        at org.eclipse.jetty.servlet.ServletHandler.doHandle(ServletHandler.java:503)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
		        at org.ops4j.pax.web.service.jetty.internal.HttpServiceServletHandler.doHandle(HttpServiceServletHandler.java:69)[111:org.ops4j.pax.web.pax-web-jetty:3.2.6]
		        at org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:137)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
		        at org.eclipse.jetty.security.SecurityHandler.handle(SecurityHandler.java:522)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
		        at org.eclipse.jetty.server.session.SessionHandler.doHandle(SessionHandler.java:231)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
		        at org.eclipse.jetty.server.handler.ContextHandler.doHandle(ContextHandler.java:1086)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
		        at org.ops4j.pax.web.service.jetty.internal.HttpServiceContext.doHandle(HttpServiceContext.java:240)[111:org.ops4j.pax.web.pax-web-jetty:3.2.6]
		        at org.eclipse.jetty.servlet.ServletHandler.doScope(ServletHandler.java:429)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
		        at org.eclipse.jetty.server.session.SessionHandler.doScope(SessionHandler.java:193)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
		        at org.eclipse.jetty.server.handler.ContextHandler.doScope(ContextHandler.java:1020)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
		        at org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:135)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
		        at org.ops4j.pax.web.service.jetty.internal.JettyServerHandlerCollection.handle(JettyServerHandlerCollection.java:75)[111:org.ops4j.pax.web.pax-web-jetty:3.2.6]
		        at org.eclipse.jetty.server.handler.HandlerWrapper.handle(HandlerWrapper.java:116)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
		        at org.eclipse.jetty.server.Server.handle(Server.java:370)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
		        at org.eclipse.jetty.server.AbstractHttpConnection.handleRequest(AbstractHttpConnection.java:494)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
		        at org.eclipse.jetty.server.AbstractHttpConnection.headerComplete(AbstractHttpConnection.java:971)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
		        at org.eclipse.jetty.server.AbstractHttpConnection$RequestHandler.headerComplete(AbstractHttpConnection.java:1033)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
		        at org.eclipse.jetty.http.HttpParser.parseNext(HttpParser.java:651)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
		        at org.eclipse.jetty.http.HttpParser.parseAvailable(HttpParser.java:235)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
		        at org.eclipse.jetty.server.AsyncHttpConnection.handle(AsyncHttpConnection.java:82)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
		        at org.eclipse.jetty.io.nio.SelectChannelEndPoint.handle(SelectChannelEndPoint.java:696)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
		        at org.eclipse.jetty.io.nio.SelectChannelEndPoint$1.run(SelectChannelEndPoint.java:53)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
		        at org.eclipse.jetty.util.thread.QueuedThreadPool.runJob(QueuedThreadPool.java:608)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
		        at org.eclipse.jetty.util.thread.QueuedThreadPool$3.run(QueuedThreadPool.java:543)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
		        at java.lang.Thread.run(Thread.java:745)[:1.8.0_45]

onos> apps -s

	*   1 org.onosproject.openflow-base    1.4.0    OpenFlow protocol southbound providers
	*   2 org.onosproject.hostprovider     1.4.0    ONOS host location provider
	*   3 org.onosproject.lldpprovider     1.4.0    ONOS LLDP link provider
	*   4 org.onosproject.openflow         1.4.0    OpenFlow southbound meta application
	    5 org.onosproject.election         1.4.0    Master election test application
	    6 org.onosproject.openstackswitching 1.4.0    SONA Openstack Switching  applications
	    7 org.onosproject.bgp              1.4.0    BGP protocol southbound providers
	    8 org.onosproject.incubator.rpc    1.4.0    ONOS inter-cluster RPC service
	    9 org.onosproject.cip              1.4.0    Cluster IP alias
	   10 org.onosproject.aaa              1.4.0    ONOS authentication application
	   11 org.onosproject.netconf          1.4.0    NetConf protocol southbound providers
	   12 org.onosproject.reactive.routing 1.4.0    SDN-IP reactive routing application
	   13 org.onosproject.flowanalyzer     1.4.0    Simple flow space analyzer
	   14 org.onosproject.mobility         1.4.0    Host mobility application
	   15 org.onosproject.messagingperf    1.4.0    Messaging performance test application
	   16 org.onosproject.vtn              1.4.0    ONOS framework applications
	   17 org.onosproject.netcfghostprovider 1.4.0    Host provider that uses network config service to discover hosts.
	   18 org.onosproject.drivermatrix     1.4.0    Driver behaviour support matric
	   19 org.onosproject.cordfabric       1.4.0    Simple fabric application for CORD
	*  20 org.onosproject.fwd              1.4.0    Reactive forwarding application using flow subsystem
	   21 org.onosproject.igmp             1.4.0    Internet Group Message Protocol
	   22 org.onosproject.pathpainter      1.4.0    Path visualization application
	*  23 org.onosproject.drivers          1.4.0    Builtin device drivers
	   24 org.onosproject.distributedprimitives 1.4.0    ONOS app to test distributed primitives
	   25 org.onosproject.olt              1.4.0    OLT application
	   26 org.onosproject.intentperf       1.4.0    Intent performance test application
	   27 org.onosproject.ovsdb            1.4.0    ONOS information providers and control/management protocol adapter
	   28 org.onosproject.cordvtn          1.4.0    Virtual tenant network service for CORD
	   29 org.onosproject.incubator.rpc.grpc 1.4.0    ONOS inter-cluster RPC based on gRPC
	*  30 org.onosproject.proxyarp         1.4.0    Proxy ARP/NDP application
	   31 org.onosproject.optical          1.4.0    Packet/Optical use-case application
	   32 org.onosproject.mfwd             1.4.0    Multicast forwarding application
	   33 org.onosproject.dhcp             1.4.0    DHCP Server application
	   34 org.onosproject.sdnip            1.4.0    SDN-IP peering application
	   35 org.onosproject.xosintegration   1.4.0    ONOS XOS integration application
	   36 org.onosproject.metrics          1.4.0    Performance metrics collection
	   37 org.onosproject.mlb              1.4.0    Balances mastership among nodes
	   38 org.onosproject.faultmanagement  1.4.0    ONOS framework applications
	   39 org.onosproject.pim              1.4.0    Protocol Independent Multicast Emulation
	   40 org.onosproject.bgprouter        1.4.0    BGP router application
	   41 org.onosproject.pcep             1.4.0    PCEP protocol southbound providers
	   42 org.onosproject.demo             1.4.0    Flow throughput test application
	   43 org.onosproject.virtualbng       1.4.0    A virtual Broadband Network Gateway(BNG) application
	   44 org.onosproject.segmentrouting   1.4.0    Segment routing application
	   45 org.onosproject.acl              1.4.0    ONOS ACL application
	   46 org.onosproject.null             1.4.0    Null southbound providers
	*  53 com.foo.app                      1.0.SNAPSHOT ONOS OSGi bundle archetype


onos> app uninstall com.foo.app 
onos> log:clear
onos> shutdown -c -cc -h

</code></pre>

## Forth Try

In this case I coppied the bundle from `/opt/onos/apache-karaf-3.0.5/system` into `/opt/onos/apache-karaf-3.0.5/deploy`. The result was the same as of second and third try.

<pre><code>
  $OCI:
	
	user@OCI:~$ ls /opt/onos/apache-karaf-3.0.5/system/org/apache/servicemix/bundles/org.apache.servicemix.bundles.kafka-clients/0.8.2.2_1/
	org.apache.servicemix.bundles.kafka-clients-0.8.2.2_1.jar
	org.apache.servicemix.bundles.kafka-clients-0.8.2.2_1.jar.sha1
	org.apache.servicemix.bundles.kafka-clients-0.8.2.2_1.pom
	org.apache.servicemix.bundles.kafka-clients-0.8.2.2_1.pom.sha1
	_remote.repositories
	
	user@OCI:~$ mkdir -p /opt/onos/apache-karaf-3.0.5/deploy/org/apache/servicemix/bundles/org.apache.servicemix.bundles.kafka-clients/0.8.2.2_1/
	user@OCI:~$ cp -r /opt/onos/apache-karaf-3.0.5/system/org/apache/servicemix/bundles/org.apache.servicemix.bundles.kafka-clients/0.8.2.2_1/* /opt/onos/apache-karaf-3.0.5/deploy/org/apache/servicemix/bundles/org.apache.servicemix.bundles.kafka-clients/0.8.2.2_1/
	
	user@OCI:~$ ls /opt/onos/apache-karaf-3.0.5/deploy/
	org  README

onos> log:clear


$ onos-app $OCI install! target/bar-1.0-SNAPSHOT.oar
	{"name":"com.foo.app","id":53,"version":"1.0.SNAPSHOT","description":"ONOS OSGi bundle archetype","origin":"Foo, Inc.","permissions":"[]","featuresRepo":"mvn:com.foo/bar/1.0-SNAPSHOT/xml/features","features":"[bar]","requiredApps":"[]","state":"ACTIVE"}


onos> log:display

	2016-03-07 11:55:03,972 | INFO  | qtp602891664-66  | ApplicationManager               | 83 - org.onosproject.onos-core-net - 1.4.0 | Application com.foo.app has been installed
	2016-03-07 11:55:03,983 | INFO  | qtp602891664-66  | FeaturesServiceImpl              | 20 - org.apache.karaf.features.core - 3.0.5 | Installing feature bar 1.0-SNAPSHOT
	2016-03-07 11:55:09,862 | WARN  | qtp602891664-66  | AetherBasedResolver              | 3 - org.ops4j.pax.logging.pax-logging-api - 1.8.4 | Error resolving artifactorg.apache.servicemix.bundles.kafka-clients:org.apache.servicemix.bundles.kafka-clients:jar:0.8.2.2_1:Could not find artifact org.apache.servicemix.bundles.kafka-clients:org.apache.servicemix.bundles.kafka-clients:jar:0.8.2.2_1 in system.repository (file:/opt/onos/apache-karaf-3.0.5/system/)
	shaded.org.eclipse.aether.resolution.ArtifactResolutionException: Could not find artifact org.apache.servicemix.bundles.kafka-clients:org.apache.servicemix.bundles.kafka-clients:jar:0.8.2.2_1 in system.repository (file:/opt/onos/apache-karaf-3.0.5/system/)
	        at shaded.org.eclipse.aether.internal.impl.DefaultArtifactResolver.resolve(DefaultArtifactResolver.java:444)
	        at shaded.org.eclipse.aether.internal.impl.DefaultArtifactResolver.resolveArtifacts(DefaultArtifactResolver.java:246)
	        at shaded.org.eclipse.aether.internal.impl.DefaultArtifactResolver.resolveArtifact(DefaultArtifactResolver.java:223)
	        at shaded.org.eclipse.aether.internal.impl.DefaultRepositorySystem.resolveArtifact(DefaultRepositorySystem.java:294)
	        at org.ops4j.pax.url.mvn.internal.AetherBasedResolver.resolve(AetherBasedResolver.java:573)
	        at org.ops4j.pax.url.mvn.internal.AetherBasedResolver.resolve(AetherBasedResolver.java:528)
	        at org.ops4j.pax.url.mvn.internal.AetherBasedResolver.resolve(AetherBasedResolver.java:506)
	        at org.ops4j.pax.url.mvn.internal.AetherBasedResolver.resolve(AetherBasedResolver.java:481)
	        at org.ops4j.pax.url.mvn.internal.Connection.getInputStream(Connection.java:123)
	        at java.net.URL.openStream(URL.java:1038)[:1.8.0_45]
	        at org.apache.karaf.features.internal.BundleManager.getInputStreamForBundle(BundleManager.java:230)
	        at org.apache.karaf.features.internal.BundleManager.doInstallBundleIfNeeded(BundleManager.java:96)
	        at org.apache.karaf.features.internal.BundleManager.installBundleIfNeeded(BundleManager.java:90)
	        at org.apache.karaf.features.internal.FeaturesServiceImpl.doInstallFeature(FeaturesServiceImpl.java:581)
	        at org.apache.karaf.features.internal.FeaturesServiceImpl.installFeatures(FeaturesServiceImpl.java:436)
	        at org.apache.karaf.features.internal.FeaturesServiceImpl.installFeature(FeaturesServiceImpl.java:417)
	        at org.apache.karaf.features.internal.FeaturesServiceImpl.installFeature(FeaturesServiceImpl.java:392)
	        at org.apache.karaf.features.internal.FeaturesServiceImpl.installFeature(FeaturesServiceImpl.java:373)
	        at org.apache.karaf.features.internal.FeaturesServiceImpl.installFeature(FeaturesServiceImpl.java:351)
	        at Proxy8410c85e_df12_49d9_9809_57f22abc3554.installFeature(Unknown Source)
	        at org.onosproject.app.impl.ApplicationManager.installAppFeatures(ApplicationManager.java:253)
	        at org.onosproject.app.impl.ApplicationManager.access$100(ApplicationManager.java:56)
	        at org.onosproject.app.impl.ApplicationManager$InternalStoreDelegate.notify(ApplicationManager.java:184)
	        at org.onosproject.app.impl.ApplicationManager$InternalStoreDelegate.notify(ApplicationManager.java:177)
	        at org.onosproject.store.app.GossipApplicationStore$InternalAppStatesListener.event(GossipApplicationStore.java:398)
	        at org.onosproject.store.ecmap.EventuallyConsistentMapImpl.lambda$notifyListeners$9(EventuallyConsistentMapImpl.java:512)
	        at org.onosproject.store.ecmap.EventuallyConsistentMapImpl$$Lambda$213/1002092238.accept(Unknown Source)
	        at java.util.concurrent.CopyOnWriteArrayList.forEach(CopyOnWriteArrayList.java:890)[:1.8.0_45]
	        at java.util.concurrent.CopyOnWriteArraySet.forEach(CopyOnWriteArraySet.java:404)[:1.8.0_45]
	        at org.onosproject.store.ecmap.EventuallyConsistentMapImpl.notifyListeners(EventuallyConsistentMapImpl.java:512)
	        at org.onosproject.store.ecmap.EventuallyConsistentMapImpl.put(EventuallyConsistentMapImpl.java:323)
	        at org.onosproject.store.app.GossipApplicationStore.activate(GossipApplicationStore.java:316)
	        at org.onosproject.store.app.GossipApplicationStore.activate(GossipApplicationStore.java:305)
	        at org.onosproject.store.app.GossipApplicationStore.activate(GossipApplicationStore.java:300)
	        at org.onosproject.app.impl.ApplicationManager.activate(ApplicationManager.java:161)
	        at org.onosproject.rest.resources.ApplicationsWebResource.installApp(ApplicationsWebResource.java:90)[135:org.onosproject.onos-rest:1.4.0]
	        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)[:1.8.0_45]
	        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)[:1.8.0_45]
	        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)[:1.8.0_45]
	        at java.lang.reflect.Method.invoke(Method.java:497)[:1.8.0_45]
	        at com.sun.jersey.spi.container.JavaMethodInvokerFactory$1.invoke(JavaMethodInvokerFactory.java:60)[129:com.sun.jersey.jersey-server:1.19.0]
	        at com.sun.jersey.server.impl.model.method.dispatch.AbstractResourceMethodDispatchProvider$ResponseOutInvoker._dispatch(AbstractResourceMethodDispatchProvider.java:205)[129:com.sun.jersey.jersey-server:1.19.0]
	        at com.sun.jersey.server.impl.model.method.dispatch.ResourceJavaMethodDispatcher.dispatch(ResourceJavaMethodDispatcher.java:75)[129:com.sun.jersey.jersey-server:1.19.0]
	        at com.sun.jersey.server.impl.uri.rules.HttpMethodRule.accept(HttpMethodRule.java:302)[129:com.sun.jersey.jersey-server:1.19.0]
	        at com.sun.jersey.server.impl.uri.rules.ResourceClassRule.accept(ResourceClassRule.java:108)[129:com.sun.jersey.jersey-server:1.19.0]
	        at com.sun.jersey.server.impl.uri.rules.RightHandPathRule.accept(RightHandPathRule.java:147)[129:com.sun.jersey.jersey-server:1.19.0]
	        at com.sun.jersey.server.impl.uri.rules.RootResourceClassesRule.accept(RootResourceClassesRule.java:84)[129:com.sun.jersey.jersey-server:1.19.0]
	        at com.sun.jersey.server.impl.application.WebApplicationImpl._handleRequest(WebApplicationImpl.java:1542)[129:com.sun.jersey.jersey-server:1.19.0]
	        at com.sun.jersey.server.impl.application.WebApplicationImpl._handleRequest(WebApplicationImpl.java:1473)[129:com.sun.jersey.jersey-server:1.19.0]
	        at com.sun.jersey.server.impl.application.WebApplicationImpl.handleRequest(WebApplicationImpl.java:1419)[129:com.sun.jersey.jersey-server:1.19.0]
	        at com.sun.jersey.server.impl.application.WebApplicationImpl.handleRequest(WebApplicationImpl.java:1409)[129:com.sun.jersey.jersey-server:1.19.0]
	        at com.sun.jersey.spi.container.servlet.WebComponent.service(WebComponent.java:409)[130:com.sun.jersey.servlet:1.19.0]
	        at com.sun.jersey.spi.container.servlet.ServletContainer.service(ServletContainer.java:558)[130:com.sun.jersey.servlet:1.19.0]
	        at com.sun.jersey.spi.container.servlet.ServletContainer.service(ServletContainer.java:733)[130:com.sun.jersey.servlet:1.19.0]
	        at javax.servlet.http.HttpServlet.service(HttpServlet.java:668)[97:org.apache.geronimo.specs.geronimo-servlet_3.0_spec:1.0.0]
	        at org.eclipse.jetty.servlet.ServletHolder.handle(ServletHolder.java:684)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.servlet.ServletHandler.doHandle(ServletHandler.java:503)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.ops4j.pax.web.service.jetty.internal.HttpServiceServletHandler.doHandle(HttpServiceServletHandler.java:69)[111:org.ops4j.pax.web.pax-web-jetty:3.2.6]
	        at org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:137)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.security.SecurityHandler.handle(SecurityHandler.java:522)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.server.session.SessionHandler.doHandle(SessionHandler.java:231)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.server.handler.ContextHandler.doHandle(ContextHandler.java:1086)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.ops4j.pax.web.service.jetty.internal.HttpServiceContext.doHandle(HttpServiceContext.java:240)[111:org.ops4j.pax.web.pax-web-jetty:3.2.6]
	        at org.eclipse.jetty.servlet.ServletHandler.doScope(ServletHandler.java:429)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.server.session.SessionHandler.doScope(SessionHandler.java:193)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.server.handler.ContextHandler.doScope(ContextHandler.java:1020)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:135)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.ops4j.pax.web.service.jetty.internal.JettyServerHandlerCollection.handle(JettyServerHandlerCollection.java:75)[111:org.ops4j.pax.web.pax-web-jetty:3.2.6]
	        at org.eclipse.jetty.server.handler.HandlerWrapper.handle(HandlerWrapper.java:116)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.server.Server.handle(Server.java:370)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.server.AbstractHttpConnection.handleRequest(AbstractHttpConnection.java:494)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.server.AbstractHttpConnection.headerComplete(AbstractHttpConnection.java:971)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.server.AbstractHttpConnection$RequestHandler.headerComplete(AbstractHttpConnection.java:1033)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.http.HttpParser.parseNext(HttpParser.java:651)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.http.HttpParser.parseAvailable(HttpParser.java:235)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.server.AsyncHttpConnection.handle(AsyncHttpConnection.java:82)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.io.nio.SelectChannelEndPoint.handle(SelectChannelEndPoint.java:696)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.io.nio.SelectChannelEndPoint$1.run(SelectChannelEndPoint.java:53)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.util.thread.QueuedThreadPool.runJob(QueuedThreadPool.java:608)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.util.thread.QueuedThreadPool$3.run(QueuedThreadPool.java:543)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at java.lang.Thread.run(Thread.java:745)[:1.8.0_45]
	Caused by: shaded.org.eclipse.aether.transfer.ArtifactNotFoundException: Could not find artifact org.apache.servicemix.bundles.kafka-clients:org.apache.servicemix.bundles.kafka-clients:jar:0.8.2.2_1 in system.repository (file:/opt/onos/apache-karaf-3.0.5/system/)
	        at shaded.org.eclipse.aether.connector.basic.ArtifactTransportListener.transferFailed(ArtifactTransportListener.java:39)
	        at shaded.org.eclipse.aether.connector.basic.BasicRepositoryConnector$TaskRunner.run(BasicRepositoryConnector.java:355)
	        at shaded.org.eclipse.aether.util.concurrency.RunnableErrorForwarder$1.run(RunnableErrorForwarder.java:67)
	        at shaded.org.eclipse.aether.connector.basic.BasicRepositoryConnector$DirectExecutor.execute(BasicRepositoryConnector.java:581)
	        at shaded.org.eclipse.aether.connector.basic.BasicRepositoryConnector.get(BasicRepositoryConnector.java:249)
	        at shaded.org.eclipse.aether.internal.impl.DefaultArtifactResolver.performDownloads(DefaultArtifactResolver.java:520)
	        at shaded.org.eclipse.aether.internal.impl.DefaultArtifactResolver.resolve(DefaultArtifactResolver.java:421)
	        ... 80 more
	2016-03-07 11:55:09,881 | WARN  | qtp602891664-66  | ApplicationManager               | 83 - org.onosproject.onos-core-net - 1.4.0 | Unable to perform operation on application com.foo.app
	java.lang.IllegalStateException: Can't install feature bar/0.0.0: 	
	Error resolving artifact org.apache.servicemix.bundles.kafka-clients:org.apache.servicemix.bundles.kafka-clients:jar:0.8.2.2_1: Could not find artifact org.apache.servicemix.bundles.kafka-clients:org.apache.servicemix.bundles.kafka-clients:jar:0.8.2.2_1 in system.repository (file:/opt/onos/apache-karaf-3.0.5/system/)
	        at org.apache.karaf.features.internal.FeaturesServiceImpl.installFeature(FeaturesServiceImpl.java:405)
	        at org.apache.karaf.features.internal.FeaturesServiceImpl.installFeature(FeaturesServiceImpl.java:373)
	        at org.apache.karaf.features.internal.FeaturesServiceImpl.installFeature(FeaturesServiceImpl.java:351)
	        at Proxy8410c85e_df12_49d9_9809_57f22abc3554.installFeature(Unknown Source)
	        at org.onosproject.app.impl.ApplicationManager.installAppFeatures(ApplicationManager.java:253)
	        at org.onosproject.app.impl.ApplicationManager.access$100(ApplicationManager.java:56)
	        at org.onosproject.app.impl.ApplicationManager$InternalStoreDelegate.notify(ApplicationManager.java:184)
	        at org.onosproject.app.impl.ApplicationManager$InternalStoreDelegate.notify(ApplicationManager.java:177)
	        at org.onosproject.store.app.GossipApplicationStore$InternalAppStatesListener.event(GossipApplicationStore.java:398)
	        at org.onosproject.store.ecmap.EventuallyConsistentMapImpl.lambda$notifyListeners$9(EventuallyConsistentMapImpl.java:512)
	        at org.onosproject.store.ecmap.EventuallyConsistentMapImpl$$Lambda$213/1002092238.accept(Unknown Source)
	        at java.util.concurrent.CopyOnWriteArrayList.forEach(CopyOnWriteArrayList.java:890)[:1.8.0_45]
	        at java.util.concurrent.CopyOnWriteArraySet.forEach(CopyOnWriteArraySet.java:404)[:1.8.0_45]
	        at org.onosproject.store.ecmap.EventuallyConsistentMapImpl.notifyListeners(EventuallyConsistentMapImpl.java:512)
	        at org.onosproject.store.ecmap.EventuallyConsistentMapImpl.put(EventuallyConsistentMapImpl.java:323)
	        at org.onosproject.store.app.GossipApplicationStore.activate(GossipApplicationStore.java:316)
	        at org.onosproject.store.app.GossipApplicationStore.activate(GossipApplicationStore.java:305)
	        at org.onosproject.store.app.GossipApplicationStore.activate(GossipApplicationStore.java:300)
	        at org.onosproject.app.impl.ApplicationManager.activate(ApplicationManager.java:161)
	        at org.onosproject.rest.resources.ApplicationsWebResource.installApp(ApplicationsWebResource.java:90)[135:org.onosproject.onos-rest:1.4.0]
	        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)[:1.8.0_45]
	        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)[:1.8.0_45]
	        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)[:1.8.0_45]
	        at java.lang.reflect.Method.invoke(Method.java:497)[:1.8.0_45]
	        at com.sun.jersey.spi.container.JavaMethodInvokerFactory$1.invoke(JavaMethodInvokerFactory.java:60)[129:com.sun.jersey.jersey-server:1.19.0]
	        at com.sun.jersey.server.impl.model.method.dispatch.AbstractResourceMethodDispatchProvider$ResponseOutInvoker._dispatch(AbstractResourceMethodDispatchProvider.java:205)[129:com.sun.jersey.jersey-server:1.19.0]
	        at com.sun.jersey.server.impl.model.method.dispatch.ResourceJavaMethodDispatcher.dispatch(ResourceJavaMethodDispatcher.java:75)[129:com.sun.jersey.jersey-server:1.19.0]
	        at com.sun.jersey.server.impl.uri.rules.HttpMethodRule.accept(HttpMethodRule.java:302)[129:com.sun.jersey.jersey-server:1.19.0]
	        at com.sun.jersey.server.impl.uri.rules.ResourceClassRule.accept(ResourceClassRule.java:108)[129:com.sun.jersey.jersey-server:1.19.0]
	        at com.sun.jersey.server.impl.uri.rules.RightHandPathRule.accept(RightHandPathRule.java:147)[129:com.sun.jersey.jersey-server:1.19.0]
	        at com.sun.jersey.server.impl.uri.rules.RootResourceClassesRule.accept(RootResourceClassesRule.java:84)[129:com.sun.jersey.jersey-server:1.19.0]
	        at com.sun.jersey.server.impl.application.WebApplicationImpl._handleRequest(WebApplicationImpl.java:1542)[129:com.sun.jersey.jersey-server:1.19.0]
	        at com.sun.jersey.server.impl.application.WebApplicationImpl._handleRequest(WebApplicationImpl.java:1473)[129:com.sun.jersey.jersey-server:1.19.0]
	        at com.sun.jersey.server.impl.application.WebApplicationImpl.handleRequest(WebApplicationImpl.java:1419)[129:com.sun.jersey.jersey-server:1.19.0]
	        at com.sun.jersey.server.impl.application.WebApplicationImpl.handleRequest(WebApplicationImpl.java:1409)[129:com.sun.jersey.jersey-server:1.19.0]
	        at com.sun.jersey.spi.container.servlet.WebComponent.service(WebComponent.java:409)[130:com.sun.jersey.servlet:1.19.0]
	        at com.sun.jersey.spi.container.servlet.ServletContainer.service(ServletContainer.java:558)[130:com.sun.jersey.servlet:1.19.0]
	        at com.sun.jersey.spi.container.servlet.ServletContainer.service(ServletContainer.java:733)[130:com.sun.jersey.servlet:1.19.0]
	        at javax.servlet.http.HttpServlet.service(HttpServlet.java:668)[97:org.apache.geronimo.specs.geronimo-servlet_3.0_spec:1.0.0]
	        at org.eclipse.jetty.servlet.ServletHolder.handle(ServletHolder.java:684)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.servlet.ServletHandler.doHandle(ServletHandler.java:503)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.ops4j.pax.web.service.jetty.internal.HttpServiceServletHandler.doHandle(HttpServiceServletHandler.java:69)[111:org.ops4j.pax.web.pax-web-jetty:3.2.6]
	        at org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:137)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.security.SecurityHandler.handle(SecurityHandler.java:522)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.server.session.SessionHandler.doHandle(SessionHandler.java:231)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.server.handler.ContextHandler.doHandle(ContextHandler.java:1086)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.ops4j.pax.web.service.jetty.internal.HttpServiceContext.doHandle(HttpServiceContext.java:240)[111:org.ops4j.pax.web.pax-web-jetty:3.2.6]
	        at org.eclipse.jetty.servlet.ServletHandler.doScope(ServletHandler.java:429)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.server.session.SessionHandler.doScope(SessionHandler.java:193)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.server.handler.ContextHandler.doScope(ContextHandler.java:1020)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:135)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.ops4j.pax.web.service.jetty.internal.JettyServerHandlerCollection.handle(JettyServerHandlerCollection.java:75)[111:org.ops4j.pax.web.pax-web-jetty:3.2.6]
	        at org.eclipse.jetty.server.handler.HandlerWrapper.handle(HandlerWrapper.java:116)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.server.Server.handle(Server.java:370)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.server.AbstractHttpConnection.handleRequest(AbstractHttpConnection.java:494)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.server.AbstractHttpConnection.headerComplete(AbstractHttpConnection.java:971)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.server.AbstractHttpConnection$RequestHandler.headerComplete(AbstractHttpConnection.java:1033)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.http.HttpParser.parseNext(HttpParser.java:651)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.http.HttpParser.parseAvailable(HttpParser.java:235)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.server.AsyncHttpConnection.handle(AsyncHttpConnection.java:82)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.io.nio.SelectChannelEndPoint.handle(SelectChannelEndPoint.java:696)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.io.nio.SelectChannelEndPoint$1.run(SelectChannelEndPoint.java:53)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.util.thread.QueuedThreadPool.runJob(QueuedThreadPool.java:608)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at org.eclipse.jetty.util.thread.QueuedThreadPool$3.run(QueuedThreadPool.java:543)[102:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	        at java.lang.Thread.run(Thread.java:745)[:1.8.0_45]
</code></pre>

## Suggestions!
If you have any suggestion I would love to hear, or merge your pull request! This repo will stay here for future reference, in case of pure soul like me finds the same problem. :)

