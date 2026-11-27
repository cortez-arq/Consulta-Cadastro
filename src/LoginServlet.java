package br.com.saas;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        String erro = request.getParameter("erro");

        response.getWriter().println("""
            <!DOCTYPE html>
            <html lang="pt-BR">
            <head>
                <meta charset="UTF-8">
                <title>Login - SaaS</title>
            </head>
            <body>
                <h1>Acesso ao sistema</h1>

                <form method="post" action="login">
                    <label>E-mail:</label><br>
                    <input type="email" name="email" required>
                    <br><br>

                    <label>Senha:</label><br>
                    <input type="password" name="senha" required>
                    <br><br>

                    <button type="submit">Entrar</button>
                </form>
            """);

        if ("1".equals(erro)) {
            response.getWriter().println(
                "<p>E-mail ou senha inválidos.</p>"
            );
        }

        response.getWriter().println("""
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

        String email = request.getParameter("email");
        String senha = request.getParameter("senha");

        if (email == null || senha == null ||
            email.isBlank() || senha.isBlank()) {

            response.sendRedirect(
                request.getContextPath() + "/login?erro=1"
            );
            return;
        }

        email = email.trim().toLowerCase();

        String sql = """
            SELECT id_usuario, nome, senha
            FROM usuarios
            WHERE email = ? AND ativo = TRUE
            """;

        try (Connection conexao = Conexao.conectar();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet resultado = stmt.executeQuery()) {

                if (resultado.next()) {

                    String hash = resultado.getString("senha");

                    if (BCrypt.checkpw(senha, hash)) {

                        HttpSession sessaoExistente =
                            request.getSession(false);

                        if (sessaoExistente != null) {
                            sessaoExistente.invalidate();
                        }

                        HttpSession sessao =
                            request.getSession(true);

                        sessao.setAttribute(
                            "usuarioId",
                            resultado.getInt("id_usuario")
                        );

                        sessao.setAttribute(
                            "usuarioNome",
                            resultado.getString("nome")
                        );

                        response.sendRedirect(
                            request.getContextPath() + "/clientes"
                        );
                        return;
                    }
                }
            }

        } catch (SQLException e) {
            throw new ServletException(
                "Erro ao consultar usuário.", e
            );
        }

        response.sendRedirect(
            request.getContextPath() + "/login?erro=1"
        );
    }
}
