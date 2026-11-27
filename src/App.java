package br.com.saas;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet("/clientes")
public class App extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        String usuarioNome = (String) request
                .getSession(false)
                .getAttribute("usuarioNome");

        String nomeBusca = request.getParameter("busca");

        if (nomeBusca == null) {
            nomeBusca = "";
        }

        nomeBusca = nomeBusca.trim();

        String sql = """
            SELECT id, nome, email
            FROM clientes
            WHERE nome LIKE ?
            ORDER BY nome
            """;

        response.getWriter().println("""
            <!DOCTYPE html>
            <html lang="pt-BR">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport"
                      content="width=device-width, initial-scale=1.0">
                <title>Gestão de Clientes</title>

                <style>
                    body {
                        font-family: Arial, sans-serif;
                        margin: 40px;
                        background: #f5f5f5;
                    }

                    .container {
                        max-width: 1000px;
                        margin: auto;
                        background: white;
                        padding: 25px;
                        border-radius: 8px;
                    }

                    input {
                        padding: 10px;
                        margin: 5px;
                        border: 1px solid #ccc;
                        border-radius: 4px;
                    }

                    button {
                        padding: 10px 15px;
                        border: none;
                        border-radius: 4px;
                        cursor: pointer;
                    }

                    .btn-primary {
                        background: #2563eb;
                        color: white;
                    }

                    .btn-secondary {
                        background: #ddd;
                        color: #222;
                    }

                    .btn-logout {
                        background: #dc2626;
                        color: white;
                    }

                    .topo {
                        display: flex;
                        justify-content: space-between;
                        align-items: center;
                        margin-bottom: 25px;
                    }

                    .usuario {
                        color: #555;
                    }

                    .secao {
                        margin-bottom: 30px;
                    }

                    table {
                        border-collapse: collapse;
                        width: 100%;
                    }

                    th, td {
                        border: 1px solid #ddd;
                        padding: 12px;
                        text-align: left;
                    }

                    th {
                        background: #eee;
                    }

                    tr:nth-child(even) {
                        background: #f9f9f9;
                    }

                    .mensagem {
                        color: #666;
                        padding: 15px 0;
                    }
                </style>
            </head>

            <body>
            <div class="container">

                <div class="topo">
                    <div>
                        <h1>Cadastro de Clientes</h1>
            """);

        response.getWriter().println(
            "<p class='usuario'>Usuário conectado: <strong>"
            + escaparHtml(usuarioNome) +
            "</strong></p>"
        );

        response.getWriter().println("""
                    </div>

                    <form method="post" action="logout">
                        <button class="btn-logout" type="submit">
                            Sair do sistema
                        </button>
                    </form>
                </div>

                <div class="secao">
                    <h2>Novo cliente</h2>

                    <form method="post" action="clientes">
                        <input type="text" name="nome"
                               placeholder="Nome do cliente" required>

                        <input type="email" name="email"
                               placeholder="E-mail" required>

                        <button class="btn-primary" type="submit">
                            Cadastrar
                        </button>
                    </form>
                </div>

                <div class="secao">
                    <h2>Pesquisar clientes</h2>

                    <form method="get" action="clientes">
                        <input type="text" name="busca"
                               placeholder="Digite o nome do cliente"
                               value="
            """);

        response.getWriter().print(escaparHtml(nomeBusca));

        response.getWriter().println("""
                        ">

                        <button class="btn-primary" type="submit">
                            Pesquisar
                        </button>

                        <a href="clientes">
                            <button class="btn-secondary" type="button">
                                Limpar
                            </button>
                        </a>
                    </form>
                </div>

                <div class="secao">
                    <h2>Clientes cadastrados</h2>

                    <table>
                        <tr>
                            <th>ID</th>
                            <th>Nome</th>
                            <th>E-mail</th>
                        </tr>
            """);

        int totalEncontrado = 0;

        try (
            Connection conexao = Conexao.conectar();
            PreparedStatement stmt = conexao.prepareStatement(sql)
        ) {

            stmt.setString(1, "%" + nomeBusca + "%");

            try (ResultSet resultado = stmt.executeQuery()) {

                while (resultado.next()) {

                    int id = resultado.getInt("id");
                    String nome = resultado.getString("nome");
                    String email = resultado.getString("email");

                    response.getWriter().println(
                        "<tr>" +
                        "<td>" + id + "</td>" +
                        "<td>" + escaparHtml(nome) + "</td>" +
                        "<td>" + escaparHtml(email) + "</td>" +
                        "</tr>"
                    );

                    totalEncontrado++;
                }
            }

        } catch (SQLException e) {
            throw new ServletException(
                "Erro ao consultar clientes.", e
            );
        }

        if (totalEncontrado == 0) {
            response.getWriter().println("""
                <tr>
                    <td colspan="3">
                        Nenhum cliente encontrado.
                    </td>
                </tr>
                """);
        }

        response.getWriter().println(
            "</table><p class='mensagem'>Total encontrado: "
            + totalEncontrado +
            "</p>"
        );

        response.getWriter().println("""
                </div>
            </div>
            </body>
            </html>
            """);
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String nome = request.getParameter("nome");
        String email = request.getParameter("email");

        if (nome == null || email == null ||
            nome.isBlank() || email.isBlank()) {

            response.sendRedirect(
                request.getContextPath() + "/clientes"
            );
            return;
        }

        nome = nome.trim();
        email = email.trim();

        String sql =
            "INSERT INTO clientes (nome, email) VALUES (?, ?)";

        try (
            Connection conexao = Conexao.conectar();
            PreparedStatement stmt = conexao.prepareStatement(sql)
        ) {

            stmt.setString(1, nome);
            stmt.setString(2, email);

            stmt.executeUpdate();

            response.sendRedirect(
                request.getContextPath() + "/clientes"
            );

        } catch (SQLException e) {
            throw new ServletException(
                "Erro ao cadastrar cliente.", e
            );
        }
    }

    private String escaparHtml(String texto) {

        if (texto == null) {
            return "";
        }

        return texto
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
    }
}
