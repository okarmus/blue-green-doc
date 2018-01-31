package com.github.scala_zookeeper

import com.twitter.conversions.time._
import com.twitter.util.{Future, JavaTimer}
import com.twitter.zk.{ZNode, ZkClient}
import org.apache.zookeeper.{CreateMode, ZooDefs}

import scala.collection.JavaConverters._



object Monitor {
  def main(args: Array[String]): Unit = {

    implicit val timer = new JavaTimer(true)
    ZkClient("localhost:2181", Some(5.seconds), 30.seconds)
      .withMode(CreateMode.EPHEMERAL)
      .withAcl(ZooDefs.Ids.OPEN_ACL_UNSAFE.asScala)
      .apply("/ActiveStack")
      .create()
      .onSuccess {
        zNode => setData(zNode).onSuccess(monitor)
      }
      .onFailure { th => println("Error: %s" + th) }

    Thread.sleep(3000)
    println("Finished")

  }

  def setData(zNode: ZNode): Future[ZNode.Data] = {
    zNode
      .setData("SomeDate".getBytes, 0)
  }

  def monitor(zNode: ZNode): Unit = {
    zNode
      .getData
      .monitor()
      .foreach(t => println(t.get()))
  }
}
