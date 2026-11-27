package br.com.saas;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;

@WebFilter("/clientes")
public class AutenticacaoFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig)
            throws ServletException {
    }

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest requisicao =
            (HttpServletRequest) request;

        HttpServletResponse resposta =
            (HttpServletResponse) response;

        HttpSession sessao =
            requisicao.getSession(false);

        if (sessao != null &&
            sessao.getAttribute("usuarioId") != null) {

            chain.doFilter(request, response);

        } else {

            resposta.sendRedirect(
                requisicao.getContextPath() + "/login"
            );
        }
    }

    @Override
    public void destroy() {
    }
}
