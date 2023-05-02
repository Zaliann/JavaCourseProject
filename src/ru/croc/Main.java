package ru.croc;

import jakarta.xml.bind.JAXBException;

import java.io.IOException;
import java.sql.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws SQLException {
        String url = "jdbc:h2:tcp://localhost:9092/./data/db";
        try (Connection conn = DriverManager.getConnection(
                url,
                "sa",
                "admin")
        ) {
            CardConnection cardConnection = new CardConnection(conn);

            Scanner scanner = new Scanner(System.in);
            String username = scanner.nextLine();

            if (username.equals("admin")) {
                admin(scanner, cardConnection);
            } else {
                user(scanner, cardConnection, username);
            }
        } catch (SQLException e) {
            throw e;
        } catch (JAXBException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Данный метод реализует админскую часть приложения, в которой можно
     * создавать, просматривать и удалять карточки
     * @param scanner сканер, для считывания со стандартного потока ввода
     * @param cardConnection  соединение с базой данных
     * @throws SQLException если возникли проблемы при работе с базой данных
     */
    private static void admin(Scanner scanner, CardConnection cardConnection) throws SQLException {
        while (true) {
            System.out.println("Actions:");
            System.out.println("1. Create card (word with options)");
            System.out.println("2. Show cards");
            System.out.println("3. Delete card");
            System.out.println("4. Exit");
            System.out.print("Choose action: ");

            int numOfAction;
            try {
                numOfAction = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                numOfAction = 0;
            }

            System.out.println();

            switch (numOfAction) {
                case 1:
                    cardConnection.createCard(scanner);
                    break;
                case 2:
                    cardConnection.showCards(scanner);
                    break;
                case 3:
                    cardConnection.deleteCard(scanner);
                    break;
                case 4:
                    System.exit(0);
                default:
                    System.out.println("No such action!");
                    System.out.println();
            }
        }
    }

    /**
     * Данный метод реализует админскую часть приложения, в которой можно
     * проходить самогенерирующиеся тесты по системе Лейтнера и экспортировать
     * результаты пользователя, то есть расположение слов в 'коробках',
     * в xml формат
     * @param scanner сканер, для считывания со стандартного потока ввода
     * @param cardConnection соединение с базой данных
     * @param username имя пользователя
     * @throws SQLException если возникли проблемы при работе с базой данных
     * @throws JAXBException если возникли проблемы при маршализации
     * @throws IOException если возникли проблемы при выводе в файл
     */
    private static void user(Scanner scanner,
                             CardConnection cardConnection,
                             String username
    ) throws SQLException, JAXBException, IOException {
        cardConnection.createUserIfAbsent(username);
        int userId = cardConnection.getUserId(username);

        while (true) {
            System.out.println("Actions:");
            System.out.println("1. Test");
            System.out.println("2. Show results");
            System.out.println("3. Exit");
            System.out.print("Choose action: ");

            int numOfAction;
            try {
                numOfAction = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                numOfAction = 0;
            }

            System.out.println();

            switch (numOfAction) {
                case 1:
                    cardConnection.runUserTesting(userId, scanner);
                    break;
                case 2:
                    cardConnection.showResults(username, userId);
                    break;
                case 3:
                    System.exit(0);
                default:
                    System.out.println("No such action!");
                    System.out.println();
            }
        }
    }
}