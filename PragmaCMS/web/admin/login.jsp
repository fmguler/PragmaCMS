<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Login</title>
        <link rel="stylesheet" href="js/bootstrap/css/bootstrap.css">
    </head>
    <body>
        <div class="container">
            <div style="height: 100px"></div>
            <div class="row">
                <div class="span2 offset2" style="margin-top:30px">
                    <img src="js/star.png">
                </div>
                <div class="span5">
                    <c:if test="${not empty errorMessage}">
                        <div class="alert alert-block alert-error">${errorMessage}</div>
                    </c:if>
                    <h1 style="margin-bottom:18px">Please Login</h1>
                    <form method="post">
                        <table class="table style-full-width">
                            <tr>
                                <td><strong>Username:</strong></td>
                                <td><input type="text" name="username"/></td>
                            </tr>
                            <tr>
                                <td><strong>Password:</strong></td>
                                <td><input type="password" name="password"/></td>
                            </tr>
                            <tr>
                                <td></td>
                                <td>
                                    <input type="submit"  value=" Login " class="btn btn-large btn-primary"/>
                                    <input type="button"  value=" Forgot Password " class="btn btn-large btn-group" onclick="alert('Not implemented yet, sorry. Send an email to cms@pragmacraft.com and we will reset your password')"/>
                                </td>
                            </tr>                            
                        </table>                        
                    </form>                    
                </div>
                <div class="span6 offset4 style-float-right">                    
                    No account? <a href="signup">Signup here!</a>
                </div>
            </div>
        </div>
    </body>
</html>
