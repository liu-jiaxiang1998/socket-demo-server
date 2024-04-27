package com.example.socketdemo;

import com.example.socketdemo.service.SocketServerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Arrays;

@SpringBootApplication
public class SocketDemoApplication implements CommandLineRunner {
    private final ApplicationContext applicationContext;

    @Autowired
    private SocketServerService socketServerService;

    public SocketDemoApplication(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(SocketDemoApplication.class);
        application.setWebApplicationType(WebApplicationType.NONE);
        application.run(args);
    }


    @Override
    public void run(String... args) throws Exception {
//        System.out.println("传递的参数为：\n");
//        Arrays.stream(args).forEach(arg -> System.out.print(arg + " "));
//		System.out.println();

        socketServerService.serve();

//		Thread.sleep(5000);
//		SpringApplication.exit(this.applicationContext);

    }

}
