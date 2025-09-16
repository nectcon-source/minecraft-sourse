package com.mojang.realmsclient.client;

import com.google.common.collect.Lists;
import com.mojang.realmsclient.dto.RegionPingResult;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Comparator;
import java.util.List;
import net.minecraft.Util;

public class Ping {
   public static List<RegionPingResult> ping(Ping.Region... var0) {
      for(Ping.Region ping : var0) {
         ping(ping.endpoint);
      }

      List<RegionPingResult> var6 = Lists.newArrayList();

      for(Ping.Region var5 : var0) {
         var6.add(new RegionPingResult(var5.name, ping(var5.endpoint)));
      }

      var6.sort(Comparator.comparingInt(RegionPingResult::ping));
      return var6;
   }

   private static int ping(String var0) {
      int var1 = 700;
      long var2x = 0L;
      Socket var4xx = null;

      for(int var5xxx = 0; var5xxx < 5; ++var5xxx) {
         try {
            SocketAddress var6xxxx = new InetSocketAddress(var0, 80);
            var4xx = new Socket();
            long var7xxxxx = now();
            var4xx.connect(var6xxxx, 700);
            var2x += now() - var7xxxxx;
         } catch (Exception var12) {
            var2x += 700L;
         } finally {
            close(var4xx);
         }
      }

      return (int)((double)var2x / 5.0);
   }

   private static void close(Socket var0) {
      try {
         if (var0 != null) {
            var0.close();
         }
      } catch (Throwable var2) {
      }
   }

   private static long now() {
      return Util.getMillis();
   }

   public static List<RegionPingResult> pingAllRegions() {
      return ping(Ping.Region.values());
   }

   static enum Region {
      US_EAST_1("us-east-1", "ec2.us-east-1.amazonaws.com"),
      US_WEST_2("us-west-2", "ec2.us-west-2.amazonaws.com"),
      US_WEST_1("us-west-1", "ec2.us-west-1.amazonaws.com"),
      EU_WEST_1("eu-west-1", "ec2.eu-west-1.amazonaws.com"),
      AP_SOUTHEAST_1("ap-southeast-1", "ec2.ap-southeast-1.amazonaws.com"),
      AP_SOUTHEAST_2("ap-southeast-2", "ec2.ap-southeast-2.amazonaws.com"),
      AP_NORTHEAST_1("ap-northeast-1", "ec2.ap-northeast-1.amazonaws.com"),
      SA_EAST_1("sa-east-1", "ec2.sa-east-1.amazonaws.com");

      private final String name;
      private final String endpoint;

      private Region(String var3, String var4) {
         this.name = var3;
         this.endpoint = var4;
      }
   }
}
