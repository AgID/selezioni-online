<div class="container">
  <div class="container-fluid">
    <div class="row-fluid row-content">
      <div class="span7">
        <p>${message('label.selezione.concorsi')}</p>
        <p><strong>${message('label.selezione.altri')}</strong></p>
      </div>
      <div class="span5">
        <form class="form-signin" action="${url.context}/rest/security/login" method="post">
          <#if _csrf??>
              <input type="hidden"
                name="${_csrf.parameterName}"
                value="${_csrf.token}"/>
          </#if>
          <legend>${message('sign.in')}</legend>
          <fieldset>
            <div class="control-group">
              <div class="controls">
                <input type="text" id="username" name="username" autocomplete="username" placeholder="${message('label.account.userName')}" class="span12">
              </div>
            </div>
            <div class="control-group">
              <div class="controls">
                <input  type="password" id="password" name="password" autocomplete="current-password" placeholder="${message('label.account.password')}" class="span12">
              </div>
            </div>
            <div class="inline-block btn-block">
                <button class="btn btn-primary span6" type="submit"><i class="icon-user icon-2x animated flash"></i> ${message('sign.in')}</button>
                <a class="btn btn-primary span6" href="${url.context}/openapi/agid-login/auth">
                    <img class="logo-header" src="res/img/agid-login.png" alt="AgID Login">
                </a>
            </div>
            <button id="passwordRecovery" class="btn btn-block" type="button"><i class="icon-envelope animated flash icon-blue"></i> <span class="text-info">${message('password.recovery')}</span></button>
            <input type="hidden" name="redirect" value="${url.context}/<#if args.redirect??>${args.redirect}<#else>home</#if>"/>
            <#if queryString??>
              <input type="hidden" name="queryString" value="${queryString}"/>
            </#if>  
            <input type="hidden" name="failure" value="${url.context}/${page.id}?failure=yes"/>
            <div>
              <small class="muted">${message('label.forgot.password.mail')}</small>
            </div>
          </fieldset>
        </form>
      </div>
    </div>
  </div>
</div> <!-- /container -->