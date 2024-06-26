<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" trimDirectiveWhitespaces="true" session="false" %>

<script>
    window.onload = function () {
        var errorAttribute = "${error}";
        if (errorAttribute && errorAttribute !== "null") {
            alert(errorAttribute);
        }
    };
</script>

<div style="margin: auto; width: 400px;">
    <div class="p-2">
        <form method="post" action="/signup.do">

            <h1 class="h3 mb-3 fw-normal">회원가입</h1>

            <div class="form-floating">
                <input type="text" name="user_id" class="form-control" id="user_id" placeholder="회원 아이디" required>
                <label for="user_id">회원 아이디</label>
                <c:if test="${not empty validate}">
                    <c:forEach var="error" items="${validate}">
                        <c:if test="${error.propertyPath == 'userId'}">
                            <span style="color: red;">${error.message}</span>
                        </c:if>
                    </c:forEach>
                </c:if>
            </div>

            <div class="form-floating">
                <input type="text" name="user_name" class="form-control" id="user_name" placeholder="회원 이름" required>
                <label for="user_id">회원 이름</label>
                <c:if test="${not empty validate}">
                    <c:forEach var="error" items="${validate}">
                        <c:if test="${error.propertyPath == 'userName'}">
                            <span style="color: red;">${error.message}</span>
                        </c:if>
                    </c:forEach>
                </c:if>
            </div>

            <div class="form-floating">
                <input type="password" name="user_password" class="form-control" id="user_password" placeholder="비밀번호"
                       required>
                <label for="user_password">비밀번호</label>
                <c:if test="${not empty validate}">
                    <c:forEach var="error" items="${validate}">
                        <c:if test="${error.propertyPath == 'password'}">
                            <span style="color: red;">${error.message}</span>
                        </c:if>
                    </c:forEach>
                </c:if>
            </div>

            <div class="form-floating">
                <input type="date" name="birthdate" class="form-control" id="birthdate" placeholder="생년월일" required>
                <label for="birthdate">생년월일</label>
                <c:if test="${not empty validate}">
                    <c:forEach var="error" items="${validate}">
                        <c:if test="${error.propertyPath == 'birth'}">
                            <span style="color: red;">${error.message}</span>
                        </c:if>
                    </c:forEach>
                </c:if>
            </div>

            <button class="w-100 btn btn-lg btn-primary mt-3" type="submit">가입하기</button>
            <p class="mt-5 mb-3 text-muted">© 2022-2024</p>
        </form>
    </div>
</div>
