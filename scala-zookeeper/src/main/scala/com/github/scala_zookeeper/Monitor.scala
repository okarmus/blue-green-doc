package com.github.scala_zookeeper

import com.twitter.conversions.time._
import com.twitter.util.{Future, JavaTimer, Try}
import com.twitter.zk.{ZNode, ZkClient}
import org.apache.zookeeper.{CreateMode, KeeperException, ZooDefs}

import scala.collection.JavaConverters._

object Monitor {
  implicit val timer: JavaTimer = new JavaTimer(true)
  val nodePath = "/ActiveStack"
  val stackId = "V2"
  val client: ZkClient =
    ZkClient("localhost:2181", Some(5.seconds), 30.seconds)
      .withMode(CreateMode.EPHEMERAL)
      .withAcl(ZooDefs.Ids.OPEN_ACL_UNSAFE.asScala)

  def main(args: Array[String]): Unit = {
    monitorNode(client(nodePath))
    while (true) {
      Thread.sleep(1000)
    }
  }

  private def monitorNode(node: ZNode): Unit = {
    node
      .getData()
      .onSuccess(monitor)
      .onFailure(createNodeAndMonitorOrFail)
  }

  private def monitor(data: ZNode.Data): Unit = {
    data.getData
      .monitor()
      .foreach(onActiveStackIdChange)
  }


  private def onActiveStackIdChange(change: Try[ZNode.Data]): Unit = {
    change
      .map(data => new String(data.bytes))
      .foreach(applyActiveStackIdChange)
  }

  private def applyActiveStackIdChange(activeStackId: String): Unit =
    activeStackId == stackId match {
      case true => println("ActiveStackId is the same as in node. Ensure that jobs are running")
      case false => println("Shutting down background due to stack id change: " + activeStackId)
    }

  private def createNode(): Future[ZNode] = {
    client.apply(nodePath)
      .create()
      .onFailure(error => throw error)
  }

  private def createNodeAndMonitorOrFail(error: Throwable) =
    error match {
      case _: KeeperException.NoNodeException => createNode().onSuccess(node => monitorNode(node))
      case _ => throw error
    }
}