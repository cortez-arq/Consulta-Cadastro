package br.com.saas;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexao {

    private static final String URL =
        "jdbc:mysql://localhost:3306/saas_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    private static final String USUARIO = "app_user";
    private static final String SENHA = "SaasNova@2026";

    public static Connection conectar() throws SQLException {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException(
                "Driver JDBC do MySQL não encontrado.", e
            );
        }

        return DriverManager.getConnection(
            URL, USUARIO, SENHA
        );
    }
}
