package mesosphere.marathon
package api.serialization

import mesosphere.UnitTest
import mesosphere.marathon.state.Container.PortMapping
import mesosphere.marathon.state.Container
import scala.collection.JavaConverters._
import mesosphere.marathon.core.pod.ContainerNetwork
import org.scalatest.Inside

class ContainerSerializerTest extends UnitTest with Inside {
  "network toMesos serializer" when {
    "a single container network is defined" should {
      "assign the portMappings to the only defined network" in {
        val networks = List(ContainerNetwork("network"))
        val container = Container.Mesos(Nil, List(PortMapping(hostPort = Some(1000))))

        val result = ContainerSerializer.toMesos(networks, container, "mesos-bridge")

        val Seq(networkInfo) = result.getNetworkInfosList.asScala.toList
        networkInfo.getName shouldBe ("network")
        inside(networkInfo.getPortMappingsList.asScala.toList) {
          case Seq(portMapping) =>
            portMapping.getHostPort shouldBe 1000
        }
      }
    }

    "multiple container networks are defined" should {
      "assign the portMappings to the specified network" in {
        val networks = List(ContainerNetwork("network-1"), ContainerNetwork("network-2"))
        val container = Container.Mesos(Nil, List(
          PortMapping(hostPort = Some(1000), networkNames = List("network-1")),
          PortMapping(hostPort = Some(1001), networkNames = List("network-2"))))

        val result = ContainerSerializer.toMesos(networks, container, "mesos-bridge")

        val Seq(networkInfo1, networkInfo2) = result.getNetworkInfosList.asScala.toList
        networkInfo1.getName shouldBe ("network-1")
        inside(networkInfo1.getPortMappingsList.asScala.toList) {
          case Seq(portMapping) =>
            portMapping.getHostPort shouldBe 1000
        }

        networkInfo2.getName shouldBe ("network-2")
        inside(networkInfo2.getPortMappingsList.asScala.toList) {
          case Seq(portMapping) =>
            portMapping.getHostPort shouldBe 1001
        }
      }
    }
  }
}
