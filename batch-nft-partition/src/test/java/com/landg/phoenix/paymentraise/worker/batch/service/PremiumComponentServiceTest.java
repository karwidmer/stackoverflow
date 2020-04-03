package com.landg.phoenix.paymentraise.worker.batch.service;

import com.landg.phoenix.paymentraise.common.constants.SQLConstants;
import com.landg.phoenix.paymentraise.worker.batch.entities.PremiumComponent;
import com.landg.phoenix.paymentraise.worker.batch.mapper.PremiumComponentRowMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Slf4j
@RunWith(SpringRunner.class)
public class PremiumComponentServiceTest {

	@MockBean
	JdbcTemplate jdbcTemplate;
	
	@MockBean
	PremiumComponentRowMapper premiumComponentRowMapper;

	@Value("jdbc:postgresql")
	private String url;

	@Value("postgres")
	private String username;

	@Value("x7Gt8Blg7")
	private String password;

	@Value("org.postgresql.Driver")
	private String driverClassName;

	@Test
	public void testListPremiumComponents() {
		log.debug("PremiumComponentServiceTest : testListPremiumComponents");

		List<PremiumComponent> premiumComponents = new ArrayList<PremiumComponent>();

		String SQL_PREMIUM_COMPONENT_QUERY = SQLConstants.PREMIUM_COMPONENT_SELECT_CLAUSE +
				SQLConstants.PREMIUM_COMPONENT_FROM_CLAUSE +
				SQLConstants.PREMIUM_COMPONENT_WHERE_CLAUSE_RANGE_MIN + Long.toString(37695) +
				SQLConstants.PREMIUM_COMPONENT_WHERE_CLAUSE_RANGE_MAX + Long.toString(56541) +
				SQLConstants.PREMIUM_COMPONENT_WHERE_CLAUSE_PART1 + "'2019-10-12'" +
				SQLConstants.PREMIUM_COMPONENT_WHERE_CLAUSE_PART2 + "'2019-11-22'" +
				SQLConstants.PREMIUM_COMPONENT_WHERE_CLAUSE_PART3 +
				SQLConstants.PREMIUM_COMPONENT_WHERE_CLAUSE_PART4 +
				SQLConstants.PREMIUM_COMPONENT_WHERE_CLAUSE_PART5 +
				SQLConstants.PREMIUM_COMPONENT_GROUP_BY_CLAUSE +
				SQLConstants.PREMIUM_COMPONENT_ORDER_BY_CLAUSE;
		log.debug("PremiumComponentServiceTest : testListPremiumComponents : SQL_PREMIUM_COMPONENT_QUERY : {}", SQL_PREMIUM_COMPONENT_QUERY);

		premiumComponents = this.jdbcTemplate.query(SQL_PREMIUM_COMPONENT_QUERY, new PremiumComponentRowMapper());

		log.debug("PaymentRaisePageItemReader : execute : premiumComponents.size() : {}", premiumComponents.size());

		assertTrue(premiumComponents.size()>0);

	}

	@Test
	public void testListPremiumComponentsQuery() {
		log.debug("PremiumComponentServiceTest : testListPremiumComponentsQuery");

		List<PremiumComponent> premiumComponents = new ArrayList<PremiumComponent>();

		String SQL_PREMIUM_COMPONENT_QUERY = SQLConstants.PREMIUM_COMPONENT_SELECT_COUNT_CLAUSE +
				SQLConstants.PREMIUM_COMPONENT_FROM_CLAUSE ;
//				SQLConstants.PREMIUM_COMPONENT_WHERE_CLAUSE_RANGE_MIN + Long.toString(37695) +
//				SQLConstants.PREMIUM_COMPONENT_WHERE_CLAUSE_RANGE_MAX + Long.toString(56541) +
//				SQLConstants.PREMIUM_COMPONENT_WHERE_CLAUSE_PART1 + "'2019-10-12'" +
//				SQLConstants.PREMIUM_COMPONENT_WHERE_CLAUSE_PART2 + "'2019-11-22'" +
//				SQLConstants.PREMIUM_COMPONENT_WHERE_CLAUSE_PART3 +
//				SQLConstants.PREMIUM_COMPONENT_WHERE_CLAUSE_PART4 +
//				SQLConstants.PREMIUM_COMPONENT_WHERE_CLAUSE_PART5 +
//				SQLConstants.PREMIUM_COMPONENT_GROUP_BY_CLAUSE +
//				SQLConstants.PREMIUM_COMPONENT_ORDER_BY_CLAUSE;
		log.debug("PremiumComponentServiceTest : testListPremiumComponents : SQL_PREMIUM_COMPONENT_QUERY : {}", SQL_PREMIUM_COMPONENT_QUERY);

		premiumComponents = jdbcTemplate.query(
				new PreparedStatementCreator() {
					public PreparedStatement createPreparedStatement(Connection connection)
							throws SQLException {
						PreparedStatement statement = connection.prepareStatement(SQL_PREMIUM_COMPONENT_QUERY);
						return statement;
					}
				}, new PremiumComponentRowMapper());


		log.debug("PaymentRaisePageItemReader : execute : premiumComponents.size() : {}", premiumComponents.size());

		assertEquals(true, premiumComponents.size()>0);

	}

	@Test
	public void testCountPremiumComponents() {
		log.debug("PremiumComponentServiceTest : testCountPremiumComponents");

		JdbcTemplate jdbcTemplate = new JdbcTemplate(testGetDataSource());

		String SQL_PREMIUM_COMPONENT_COUNT_QUERY = SQLConstants.PREMIUM_COMPONENT_SELECT_DISTINCT_COUNT_CLAUSE +
				SQLConstants.PREMIUM_COMPONENT_SELECT_COUNT_WHERE_CLAUSE_PART1 +
				SQLConstants.PREMIUM_COMPONENT_SELECT_COUNT_WHERE_CLAUSE_PART2 + "'2019-10-12'" +
				SQLConstants.PREMIUM_COMPONENT_SELECT_COUNT_WHERE_CLAUSE_PART3 + "'2019-11-22'" +
				SQLConstants.PREMIUM_COMPONENT_SELECT_COUNT_WHERE_CLAUSE_PART4 +
				SQLConstants.PREMIUM_COMPONENT_SELECT_COUNT_WHERE_CLAUSE_PART5 +
				SQLConstants.PREMIUM_COMPONENT_SELECT_COUNT_WHERE_CLAUSE_PART6 +
				SQLConstants.PREMIUM_COMPONENT_SELECT_COUNT_GROUP_BY_CLAUSE;
		log.debug("PremiumComponentServiceTest : testCountPremiumComponents : SQL_PREMIUM_COMPONENT_COUNT_QUERY : {}", SQL_PREMIUM_COMPONENT_COUNT_QUERY);

		int premiumComponentCount = jdbcTemplate.queryForObject(SQL_PREMIUM_COMPONENT_COUNT_QUERY, Integer.class);

		log.debug("PremiumComponentServiceTest : testCountPremiumComponents : premiumComponentCount : {}", premiumComponentCount);

		assertEquals(true, premiumComponentCount>0);

	}

	private DriverManagerDataSource testGetDataSource() {
		log.debug("PhoenixPaymentraiseBatchConnectionTests : testDataSource()");

		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName(this.driverClassName);
		dataSource.setUrl(this.url);
		dataSource.setUsername(this.username);
		dataSource.setPassword(this.password);

		return dataSource;
	}
}
	
	

