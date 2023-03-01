package com.example.project.security.config.jwt;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.example.project.dao.User;
import com.example.project.repository.UserRepository;
import com.example.project.security.config.auth.PrincipalDetails;

// ��ť��Ƽ�� filter�� ������ �ִµ� �� �����߿� BasicAuthenticationFilter��� ���� ����
// �����̳� ������ �ʿ��� Ư�� �ּҸ� ��û���� �� �� ���͸� ������ Ÿ�� �Ǿ�����
// ���� ���� �� ������ �ʿ��� �ּҰ� �ƴϸ� �� ���͸� Ÿ���ʴ´�.
public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

	private UserRepository userRepository;

	public JwtAuthorizationFilter(AuthenticationManager authenticationManager, UserRepository userRepository) {
		super(authenticationManager);
		this.userRepository = userRepository;
	}

	// �����̳� ������ �ʿ��� �ּҿ�û�� ���� �� �ش� ���͸� Ÿ�� �ȴ�.
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		System.out.println("�����̳� ������ �ʿ��� �ּ� ��û ��");
		String jwtHeader = request.getHeader(JwtProperties.HEADER_STRING);
		System.out.println("jwtHeader : " + jwtHeader);

		// header�� �ִ��� Ȯ��
		if (jwtHeader == null || !jwtHeader.startsWith(JwtProperties.TOKEN_PREFIX)) {
			chain.doFilter(request, response);
			return;
		}
		try {
			// JWT ��ū�� ������ �ؼ� �������� ��������� Ȯ��
			String jwtToken = request.getHeader(JwtProperties.HEADER_STRING).replace(JwtProperties.TOKEN_PREFIX, "");
			String userid = JWT.require(Algorithm.HMAC512(JwtProperties.SECRET)).build().verify(jwtToken)
					.getClaim("userid").asString();

			System.out.println(userid);
			// ���Ѱ���
			if (userid != null) {
				User userEntity = userRepository.findByUserid(userid);

				PrincipalDetails principalDetails = new PrincipalDetails(userEntity);

				// Jwt ��ū ������ ���ؼ� ������ �����̸� Authentication ��ü�� ����� �ش�.
				Authentication authentication = new UsernamePasswordAuthenticationToken(principalDetails, null,
						principalDetails.getAuthorities());

				// ������ ��ť��Ƽ�� ���ǿ� �����Ͽ� Authentication ��ü�� ����
				SecurityContextHolder.getContext().setAuthentication(authentication);
				chain.doFilter(request, response);
			}
		} catch (TokenExpiredException e) {
			response.getWriter().print(e);
		}
	}
}