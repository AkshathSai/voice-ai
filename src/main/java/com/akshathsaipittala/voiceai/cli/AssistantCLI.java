package com.akshathsaipittala.voiceai.cli;

import com.akshathsaipittala.voiceai.service.AssistantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Scanner;

@Component
public class AssistantCLI implements CommandLineRunner {

    private final AssistantService assistantService;

    @Autowired
    public AssistantCLI(AssistantService assistantService) {
        this.assistantService = assistantService;
    }

    @Override
    public void run(String... args) throws Exception {
        if (args.length > 0 && args[0].equals("--cli")) {
            startCLI();
        }
    }

    private void startCLI() {
        System.out.println("=== Voice AI Assistant CLI ===");
        System.out.println("Commands: start, stop, status, help, exit");
        System.out.println("Or type any message to process it as a voice command");

        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();

            switch (input.toLowerCase()) {
                case "start":
                    assistantService.start();
                    System.out.println("Assistant started");
                    break;
                case "stop":
                    assistantService.stop();
                    System.out.println("Assistant stopped");
                    break;
                case "status":
                    System.out.println("Running: " + assistantService.isRunning());
                    System.out.println("Listening: " + assistantService.isListening());
                    break;
                case "help":
                    printHelp();
                    break;
                case "exit":
                case "quit":
                    running = false;
                    assistantService.stop();
                    System.out.println("Goodbye!");
                    break;
                default:
                    if (!input.isEmpty()) {
                        String response = assistantService.processCommand(input);
                        System.out.println("Assistant: " + response);
                    }
                    break;
            }
        }

        scanner.close();
    }

    private void printHelp() {
        System.out.println("Available commands:");
        System.out.println("  start  - Start the voice assistant");
        System.out.println("  stop   - Stop the voice assistant");
        System.out.println("  status - Show assistant status");
        System.out.println("  help   - Show this help message");
        System.out.println("  exit   - Exit the CLI");
        System.out.println();
        System.out.println("Voice commands you can try:");
        System.out.println("  'remind me to call John at 3 PM'");
        System.out.println("  'what's the weather like'");
        System.out.println("  'I ate a sandwich for lunch'");
        System.out.println("  'set a timer for 5 minutes'");
        System.out.println("  'help me debug this code'");
    }
}
