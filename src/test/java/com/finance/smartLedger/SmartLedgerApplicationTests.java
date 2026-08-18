package com.finance.smartLedger;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
  "spring.jpa.hibernate.ddl-auto=none",
  "spring.jpa.hibernate.hbm2ddl.auto=none"
})
class SmartLedgerApplicationTests {

  @Test
  void contextLoads() {}
}
