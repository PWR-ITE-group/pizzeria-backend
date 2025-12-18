package pl.edu.pwr.pizzeria.pizzeriabackend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.users.Employee;
import pl.edu.pwr.pizzeria.pizzeriabackend.repository.users.EmployeeRepository;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final pl.edu.pwr.pizzeria.pizzeriabackend.security.JwtService jwtService;
    private final EmployeeRepository employeeRepository;

    public JwtAuthenticationFilter(pl.edu.pwr.pizzeria.pizzeriabackend.security.JwtService jwtService, EmployeeRepository employeeRepository) {
        this.jwtService = jwtService;
        this.employeeRepository = employeeRepository;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userLogin;

        // 1. Проверяем заголовок (должен начинаться с "Bearer ")
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        userLogin = jwtService.extractLogin(jwt);

        // 2. Если логин есть, а аутентификации в контексте еще нет
        if (userLogin != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Ищем пользователя в БД
            Employee employee = employeeRepository.findByLogin(userLogin).orElse(null);

            if (employee != null && jwtService.isTokenValid(jwt, employee)) {
                // 3. Создаем объект UserDetails (адаптер для Spring Security)
                UserDetails userDetails = User.builder()
                        .username(employee.getLogin())
                        .password(employee.getPasswordHash())
                        .roles(employee.getRole().toUpperCase()) // Важно для @PreAuthorize
                        .build();

                // 4. Кладем пользователя в контекст безопасности (он "зашел")
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}
