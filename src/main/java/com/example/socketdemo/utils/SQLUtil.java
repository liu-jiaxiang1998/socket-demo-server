package com.example.socketdemo.utils;

import com.alibaba.druid.pool.DruidDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

/**
 * 数据库工具类
 * 测试通过，可以正常使用！
 * date: 2024/5/7
 * author: ljx
 */
@Slf4j
public class SQLUtil {
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/zmkj";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "admin123";

    private static DataSource dataSource;
    private static JdbcTemplate jdbcTemplate;
    private static TransactionTemplate transactionTemplate;

    public static void init() {
        dataSource = setupDataSource();
        jdbcTemplate = new JdbcTemplate(dataSource);
        transactionTemplate = new TransactionTemplate(new DataSourceTransactionManager(dataSource));
        log.info("数据库连接池成功初始化");
    }

    private static DataSource setupDataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl(URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);
        dataSource.setInitialSize(5);
        dataSource.setMaxActive(10);
        return dataSource;
    }

    public static void executeUpdateInTransaction(String sql) {
        Boolean isCompleted = transactionTemplate.execute(new TransactionCallback<Boolean>() {
            @Override
            public Boolean doInTransaction(TransactionStatus status) {
//                log.info("status.isRollbackOnly() " + status.isRollbackOnly());
//                log.info("status.isCompleted() " + status.isCompleted());
//                log.info("status.hasTransaction() " + status.hasTransaction());
                jdbcTemplate.update(sql);
//                status.setRollbackOnly();
//                status.flush();

                // 注册事务同步回调
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        // 在事务提交后执行的逻辑
                        log.info("---事务已提交---");
                    }

                    @Override
                    public void afterCompletion(int status) {
                        log.info("status " + status);
                        if (status == TransactionSynchronization.STATUS_COMMITTED) {
                            // 在事务完成后（包括提交）执行的逻辑
                            log.info("---事务完成---");
                        } else {
                            // 在事务回滚后执行的逻辑
                            log.info("---事务回滚---");
                        }
                    }
                });

                return true;
            }
        });
    }

    /**
     * 超级智障的一个方法！！吐了！！
     */
    public static void executeSpecialUpdateInTransaction(String nian, String yue, String ri, String shi, String fen, String passtime, String picture_namece, String picture_namefront, String whp, String color, String speed, String chepai,
                                                         String chedaohao, String zhoushu, String zhouzushu, String zhouzuxihao, String zhouzuluntai, String chengchang, String chekuan, String chegao, String xiangchang, String xiangkuan,
                                                         String xianggao, String lidigao, String lb_chang, String lb_gao, String manzai, String chaoxianlv, String cx50, String cx75, String cx100, String fugai, String manhuo, String cz_huowu, String chaozhong, String gaizhuang1, String chaochang1, String chaokuan1, String chaogao1,
                                                         String cz49, String cz60, String cz75, String weight_cz, String weight, String crop_zb, String head_zb, String tyre_zb, String zhengpath, String cepath, String croppath, String headpath, String video_addr) {
        Boolean isCompleted = transactionTemplate.execute(new TransactionCallback<Boolean>() {
            @Override
            public Boolean doInTransaction(TransactionStatus status) {
//                log.info("status.isRollbackOnly() " + status.isRollbackOnly());
//                log.info("status.isCompleted() " + status.isCompleted());
//                log.info("status.hasTransaction() " + status.hasTransaction());
                String sql = "INSERT INTO car_infor (year,month,day,hour,minute,passtime,name,namefront,whp,color,speed,chepai," +
                        "chedaohao,zhoushu,zhouzushu,zhouzuxuhao,zhouzuluntai,chechang,chekuan,chegao,xiangchang,xiangkuan," +
                        "xianggao,lidigao,lanbanchang,lanbangao,manzailv,chaoxianlv,cx50,cx75,cx100,fugai,manhuo,cz_huowu,chaozhong,gaizhuang,chaochang,chaokuan,chaogao," +
                        "cz49,cz60,cz75,chengzhong,weight,crop_zb,head_zb,tyre_zb,picture_zl,picture_cl,picture_crop,picture_head,video) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?," +
                        "?,?,?,?,?,?,?,?,?,?," +
                        "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?," +
                        "?,?,?,?,?,?,?,?,?,?,?,?,?)";
                jdbcTemplate.update(sql, nian, yue, ri, shi, fen, passtime, picture_namece, picture_namefront, whp, color, speed, chepai,
                        chedaohao, zhoushu, zhouzushu, zhouzuxihao, zhouzuluntai, chengchang, chekuan, chegao, xiangchang, xiangkuan,
                        xianggao, lidigao, lb_chang, lb_gao, manzai, chaoxianlv, cx50, cx75, cx100, fugai, manhuo, cz_huowu, chaozhong, gaizhuang1, chaochang1, chaokuan1, chaogao1,
                        cz49, cz60, cz75, weight_cz, weight, crop_zb, head_zb, tyre_zb, zhengpath, cepath, croppath, headpath, video_addr);
//                status.setRollbackOnly();
//                status.flush();

                // 注册事务同步回调
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        // 在事务提交后执行的逻辑
                        log.info("---事务已提交---");
                    }

                    @Override
                    public void afterCompletion(int status) {
                        log.info("status " + status);
                        if (status == TransactionSynchronization.STATUS_COMMITTED) {
                            // 在事务完成后（包括提交）执行的逻辑
                            log.info("---事务完成---");
                        } else {
                            // 在事务回滚后执行的逻辑
                            log.info("---事务回滚---");
                        }
                    }
                });

                return true;
            }
        });
    }

    public static List<Map<String, Object>> executeQuery(String sql) {
        return jdbcTemplate.queryForList(sql);
    }
}
