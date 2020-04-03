package com.landg.phoenix.paymentraise.common;

import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.jms.*;
import javax.sql.DataSource;
import java.sql.SQLException;


//Local testing connections only as current pipeline does not

@Slf4j
@RunWith(SpringRunner.class)
public class PhoenixPaymentraiseBatchConnectionTests {

	@Value("jdbc:postgresql")
	private String url;

	@Value("postgres")
	private String username;

	@Value("x7Gt8Blg7")
	private String password;

	@Value("org.postgresql.Driver")
	private String driverClassName;

	@Test
	 public void testActiveMQ() throws Exception {
		log.debug("PhoenixPaymentraiseBatchConnectionTests : testActiveMQ()");

		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
		connectionFactory.setBrokerURL("tcp://localhost:61616");
		connectionFactory.setUserName("admin");
		connectionFactory.setPassword("admin");
		connectionFactory.setTrustAllPackages(true);

		final Connection connection = connectionFactory.createConnection();

		connection.start();

		final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Long unixTime = System.currentTimeMillis() / 1000L;

		final Queue queue = session.createQueue("PR-BATCH-REQ"+unixTime.toString());

		{
		   final MessageProducer producer = session.createProducer(queue);
		   final TextMessage message = session.createTextMessage("testing");
		   producer.send(message);
		}

		{
		   final MessageConsumer consumer = session.createConsumer(queue);
		   final TextMessage message = (TextMessage) consumer.receive();
//		   Assert.assertNotNull(message);
		   Assert.assertEquals("testing", message.getText());
		}
	 }

	@Test
	public void testDataSource(){
		log.debug("PhoenixPaymentraiseBatchConnectionTests : testDataSource()");

		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName(this.driverClassName);
		dataSource.setUrl(this.url);
		dataSource.setUsername(this.username);
		dataSource.setPassword(this.password);

		Assert.assertNotNull(dataSource);

		java.sql.Connection dataSourceConnection = null;
		try {
			dataSourceConnection = dataSource.getConnection();
			Assert.assertNotNull(dataSourceConnection);
		}
		catch (SQLException e) {
			log.error("Datasource connection failed");
		}

	}

}
