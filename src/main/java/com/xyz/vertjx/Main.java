package com.xyz.vertjx;

import com.xyz.vertjx.demo1.CollectionService;
import com.xyz.vertjx.demo1.HeatSensor;
import com.xyz.vertjx.demo1.RxCollectionService;
import com.xyz.vertjx.demo1.SnapshotService;
import com.xyz.vertktx.KtCollectionVerticle;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.impl.ConcurrentHashSet;

import java.util.Set;

public class Main {

  public static final Set<Integer> ports = new ConcurrentHashSet<>();

  public static void main(String[] args) {
    System.out.println("Java Main");
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(HeatSensor.class,new DeploymentOptions().setInstances(5));
    vertx.deployVerticle(CollectionService.class, new DeploymentOptions().setInstances(1));
    vertx.deployVerticle(SnapshotService.class, new DeploymentOptions().setInstances(1));
    vertx.deployVerticle(new KtCollectionVerticle());
    Single<String> stringSingle = io.vertx.rxjava3.core.Vertx.vertx().deployVerticle(new RxCollectionService());
    stringSingle.subscribe();
  }
}
