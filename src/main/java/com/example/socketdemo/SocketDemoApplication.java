package com.example.socketdemo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SocketDemoApplication implements CommandLineRunner {

//    @Autowired
//    private SocketServerService socketServerService;

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(SocketDemoApplication.class);
        application.setWebApplicationType(WebApplicationType.NONE);
        application.run(args);
    }


    @Override
    public void run(String... args) throws Exception {
//        System.out.println("传递的参数为：\n");
//        Arrays.stream(args).forEach(arg -> System.out.print(arg + " "));
//        System.out.println();
//        socketServerService.serve();


    }

}
