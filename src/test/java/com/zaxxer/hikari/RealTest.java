package com.zaxxer.hikari;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;

public class RealTest {

   HikariDataSource hikariDataSource = new HikariDataSource();

   @Before
   public void before(){
      hikariDataSource.setUsername("sa");
      hikariDataSource.setPassword("sa");
      hikariDataSource.setDriverClassName("org.h2.Driver");
      hikariDataSource.setJdbcUrl("jdbc:h2:mem:testDb");
      hikariDataSource.setMaxLifetime(30_000);
      hikariDataSource.setMaximumPoolSize(2);
      hikariDataSource.setMinimumIdle(2);
      hikariDataSource.setLeakDetectionThreshold(2000);
   }

   @Test
   public void test1() throws Exception{
      try(
         // 模拟未还连接
         Connection connection = hikariDataSource.getConnection();
         ) {
         Thread.sleep(3000);
      }
   }

   @After
   public void after(){
      hikariDataSource.close();
   }

}
