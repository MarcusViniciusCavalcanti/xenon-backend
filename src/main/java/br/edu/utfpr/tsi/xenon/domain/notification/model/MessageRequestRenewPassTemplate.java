package br.edu.utfpr.tsi.xenon.domain.notification.model;

public record MessageRequestRenewPassTemplate(String to,
                                              String url) implements EmailTemplate {

    private static final String TEMPLATE;

    static {
        TEMPLATE = """
            <!DOCTYPE
                   html
                   PUBLIC "-//W3C//DTD XHTML 1.0 Transitional
                   //EN" "https://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
                   <html
                   xmlns="https://www.w3.org/1999/xhtml"
                   xmlns:v="urn:schemas-microsoft-com:vml"
                   xmlns:o="urn:schemas-microsoft-com:office:office">
                   
                   <head>
                   <!--[if gte mso 9]><xml>
                   <o:OfficeDocumentSettings><o:AllowPNG/>
                   <o:PixelsPerInch>96</o:PixelsPerInch>
                   </o:OfficeDocumentSettings></xml><![endif]-->
                   <meta http-equiv="Content-Type"
                   content="text/html; charset=utf-8">
                   <meta name="viewport"
                   content="width=device-width">
                   <!--[if !mso]><!-->
                   <meta http-equiv="X-UA-Compatible"
                   content="IE=edge">
                   <!--<![endif]-->
                   <title></title>
                   <!--[if !mso]><!-->
                   <link href="https://fonts.googleapis.com/css?family=Abril+Fatface"
                   rel="stylesheet" type="text/css">
                   <link href="https://fonts.googleapis.com/css?family=Alegreya"
                   rel="stylesheet" type="text/css">
                   <link href="https://fonts.googleapis.com/css?family=Arvo"
                   rel="stylesheet" type="text/css">
                   <link href="https://fonts.googleapis.com/css?family=Bitter"
                   rel="stylesheet" type="text/css">
                   <link href="https://fonts.googleapis.com/css?family=Cabin"
                   rel="stylesheet" type="text/css">
                   <link href="https://fonts.googleapis.com/css?family=Ubuntu"
                   rel="stylesheet" type="text/css">
                   <!--<![endif]-->
                   <style type="text/css">
                   body {
                   margin: 0;
                   padding: 0;
                   }
                   
                   table,
                   td,
                   tr {
                   vertical-align: top;
                   border-collapse: collapse;
                   }
                   
                   * {
                   line-height: inherit;
                   }
                   
                   a[x-apple-data-detectors=true] {
                   color: inherit !important;
                   text-decoration: none !important;
                   }
                   </style>
                   <style type="text/css" id="media-query">
                   @media (max-width: 520px) {
                   
                   .block-grid,
                   .col {
                   min-width: 320px !important;
                   max-width: 100% !important;
                   display: block !important;
                   }
                   
                   .block-grid {
                   width: 100% !important;
                   }
                   
                   .col {
                   width: 100% !important;
                   }
                   
                   .col_cont {
                   margin: 0 auto;
                   }
                   
                   img.fullwidth,
                   img.fullwidthOnMobile {
                   width: 100% !important;
                   }
                   
                   .no-stack .col {
                   min-width: 0 !important;
                   display: table-cell !important;
                   }
                   
                   .no-stack.two-up .col {
                   width: 50% !important;
                   }
                   
                   .no-stack .col.num2 {
                   width: 16.6% !important;
                   }
                   
                   .no-stack .col.num3 {
                   width: 25% !important;
                   }
                   
                   .no-stack .col.num4 {
                   width: 33% !important;
                   }
                   
                   .no-stack .col.num5 {
                   width: 41.6% !important;
                   }
                   
                   .no-stack .col.num6 {
                   width: 50% !important;
                   }
                   
                   .no-stack .col.num7 {
                   width: 58.3% !important;
                   }
                   
                   .no-stack .col.num8 {
                   width: 66.6% !important;
                   }
                   
                   .no-stack .col.num9 {
                   width: 75% !important;
                   }
                   
                   .no-stack .col.num10 {
                   width: 83.3% !important;
                   }
                   
                   .video-block {
                   max-width: none !important;
                   }
                   
                   .mobile_hide {
                   min-height: 0px;
                   max-height: 0px;
                   max-width: 0px;
                   display: none;
                   overflow: hidden;
                   font-size: 0px;
                   }
                   
                   .desktop_hide {
                   display: block !important;
                   max-height: none !important;
                   }
                   
                   .img-container.big img {
                   width: auto !important;
                   }
                   }
                   </style>
                   </head>
                   
                   <body class="clean-body" style="margin: 0;
                   padding: 0; -webkit-text-size-adjust: 100%;
                   background-color: #FFFFFF;">
                   <!--[if IE]><div class="ie-browser"><![endif]-->
                   <table class="nl-container"
                   style="table-layout: fixed; vertical-align: top;
                   min-width: 320px; border-spacing: 0;
                   border-collapse: collapse;
                   mso-table-lspace: 0pt; mso-table-rspace: 0pt;
                   background-color: #FFFFFF; width: 100%;"
                   cellpadding="0" cellspacing="0"
                   role="presentation" width="100%"
                   bgcolor="#FFFFFF" valign="top">
                   <tbody>
                   <tr style="vertical-align: top;" valign="top">
                   <td style="word-break: break-word;
                   vertical-align: top;"
                   valign="top">
                   <!--[if (mso)|(IE)]><table width="100%"
                   cellpadding="0"
                   cellspacing="0" border="0">
                   <tr><td align="center"
                   style="background-color:#FFFFFF"><![endif]-->
                   <div style="background-color:#f5f5f5;">
                   <div class="block-grid " style="min-width: 320px;
                   max-width: 500px; overflow-wrap: break-word;
                   word-wrap: break-word; word-break: break-word;
                   Margin: 0 auto; background-color: transparent;">
                   <div style="border-collapse: collapse;
                   display: table;width: 100%;background-color:transparent;">
                   <!--[if (mso)|(IE)]><table width="100%"
                   cellpadding="0"
                   cellspacing="0"
                   border="0"
                   style="background-color:#f5f5f5;"
                   ><tr><td align="center"><table cellpadding="0"
                   cellspacing="0"
                   border="0"
                   style="width:500px">
                   <tr class="layout-full-width"
                   style="background-color:transparent"><![endif]-->
                   <!--[if (mso)|(IE)]><td align="center"
                   width="500"
                   style="background-color:transparent;
                   width:500px; border-top: 0px solid transparent;\040
                   border-left: 0px solid transparent;
                   border-bottom: 0px solid transparent;
                   border-right: 0px solid transparent;"
                   valign="top"><table width="100%"
                   cellpadding="0"
                   cellspacing="0" border="0"><tr>
                   <td style="padding-right: 0px; padding-left: 0px;
                   padding-top:5px; padding-bottom:0px;"><![endif]-->
                   <div class="col num12" style="min-width: 320px;
                   max-width: 500px; display: table-cell;
                   vertical-align: top; width: 500px;">
                   <div class="col_cont" style="width:100% !important;">
                   <!--[if (!mso)&(!IE)]><!-->
                   <div style="border-top:0px solid transparent;
                   border-left:0px solid transparent;
                   border-bottom:0px solid transparent;
                   border-right:0px solid transparent; padding-top:5px;
                   padding-bottom:0px; padding-right: 0px;
                   padding-left: 0px;">
                   <!--<![endif]-->
                   <table class="divider" border="0" cellpadding="0"
                   cellspacing="0" width="100%"
                   style="table-layout: fixed; vertical-align: top;
                   border-spacing: 0; border-collapse: collapse;
                   mso-table-lspace: 0pt; mso-table-rspace: 0pt;
                   min-width: 100%; -ms-text-size-adjust: 100%;
                   -webkit-text-size-adjust: 100%;"
                   role="presentation" valign="top">
                   <tbody>
                   <tr style="vertical-align: top;" valign="top">
                   <td class="divider_inner" style="word-break: break-word;
                   vertical-align: top; min-width: 100%;
                   -ms-text-size-adjust: 100%;
                   -webkit-text-size-adjust: 100%; padding-top: 10px; padding-right: 10px;
                   padding-bottom: 10px; padding-left: 10px;" valign="top">
                   <table class="divider_content" border="0" cellpadding="0"
                   cellspacing="0" width="100%"\s
                   style="table-layout: fixed;
                   vertical-align: top;\s
                   border-spacing: 0; border-collapse: collapse;
                   mso-table-lspace: 0pt; mso-table-rspace: 0pt;
                   border-top: 1px solid #BBBBBB; width: 100%;"
                   align="center" role="presentation" valign="top">
                   <tbody>
                   <tr style="vertical-align: top;" valign="top">
                   <td style="word-break: break-word; vertical-align: top;
                   -ms-text-size-adjust: 100%;
                   -webkit-text-size-adjust: 100%;" valign="top"><span></span></td>
                   </tr>
                   </tbody>
                   </table>
                   </td>
                   </tr>
                   </tbody>
                   </table>
                   <div class="img-container center autowidth big"\s
                   align="center" style="padding-right: 0px;
                   padding-left: 0px;">
                   <!--[if mso]><table width="100%" cellpadding="0"
                   cellspacing="0" border="0">
                   <tr style="line-height:0px"><td style="padding-right: 0px;
                   padding-left: 0px;" align="center"><![endif]--><img class="center autowidth"\s
                   align="center"
                   border="0"\s
                   src="https://d1oco4z2z1fhwp.cloudfront.net/templates/default/2966/Top.png"\s
                   style="text-decoration: none;
                   -ms-interpolation-mode: bicubic;
                   height: auto; border: 0; width: 500px;\s
                   max-width: 100%; display: block;" width="500">
                   <!--[if mso]></td></tr></table><![endif]-->
                   </div>
                   <!--[if (!mso)&(!IE)]><!-->
                   </div>
                   <!--<![endif]-->
                   </div>
                   </div>
                   <!--[if (mso)|(IE)]></td></tr></table><![endif]-->
                   <!--[if (mso)|(IE)]></td></tr></table></td></tr></table><![endif]-->
                   </div>
                   </div>
                   </div>
                   <div style="background-color:#f5f5f5;">
                   <div class="block-grid " style="min-width: 320px;
                   max-width: 500px; overflow-wrap: break-word; word-wrap: break-word;
                   word-break: break-word; Margin: 0 auto; background-color: #ffffff;">
                   <div style="border-collapse: collapse;
                   display: table;width: 100%;background-color:#ffffff;">
                   <!--[if (mso)|(IE)]><table width="100%" cellpadding="0"
                   cellspacing="0" border="0"\s
                   style="background-color:#f5f5f5;"><tr><td align="center">
                   <table cellpadding="0" cellspacing="0" border="0"\s
                   style="width:500px"><tr class="layout-full-width"\040
                   style="background-color:#ffffff"><![endif]-->
                   <!--[if (mso)|(IE)]><td align="center" width="500"\s
                   style="background-color:#ffffff;width:500px;
                   border-top: 0px solid transparent;\s
                   border-left: 0px solid transparent; border-bottom: 0px solid transparent;
                   border-right: 0px solid transparent;" valign="top">
                   <table width="100%" cellpadding="0" cellspacing="0" border="0"><tr>
                   <td style="padding-right: 0px; padding-left: 0px; padding-top:0px;
                   padding-bottom:5px;"><![endif]-->
                   <div class="col num12" style="min-width: 320px;
                   max-width: 500px; display: table-cell; vertical-align: top;
                   width: 500px;">
                   <div class="col_cont" style="width:100% !important;">
                   <!--[if (!mso)&(!IE)]><!-->
                   <div style="border-top:0px solid transparent;
                   border-left:0px solid transparent; border-bottom:0px solid transparent;
                   border-right:0px solid transparent; padding-top:0px; padding-bottom:5px;
                   padding-right: 0px; padding-left: 0px;">
                   <!--<![endif]-->
                   <div class="img-container center autowidth"
                   align="center"
                   style="padding-right: 0px;padding-left: 0px;">
                   <!--[if mso]><table width="100%" cellpadding="0" cellspacing="0"
                   border="0"><tr style="line-height:0px">
                   <td style="padding-right: 0px;padding-left: 0px;"\s
                   align="center"><![endif]--><img class="center autowidth"\s
                   align="center" border="0"\s
                   src="https://res.cloudinary.com/xenonapp/image/upload/v1635709885/avatar/logo-white-bg_2x_gosksu.png"
                   style="text-decoration: none; -ms-interpolation-mode: bicubic;\s
                   height: auto; border: 0; width: 162px; max-width: 100%;
                   display: block;" width="162">
                   <!--[if mso]></td></tr></table><![endif]-->
                   </div>
                   <div class="img-container center fixedwidth" align="center"
                   style="padding-right: 5px;padding-left: 5px;">
                   <!--[if mso]><table width="100%" cellpadding="0" cellspacing="0"
                   border="0"><tr style="line-height:0px">
                   <td style="padding-right: 5px;padding-left: 5px;"\s
                   align="center"><![endif]--><img class="center fixedwidth"\s
                   align="center" border="0"
               src="https://d1oco4z2z1fhwp.cloudfront.net/templates/default/2966/pass-animate.gif"
                   alt="reset-password" title="reset-password"
                   style="text-decoration: none; -ms-interpolation-mode: bicubic;\s
                   height: auto; border: 0; width: 350px; max-width: 100%;
                   display: block;" width="350">
                   <div style="font-size:1px;line-height:5px">&nbsp;</div>
                   <!--[if mso]></td></tr></table><![endif]-->
                   </div>
                   <table cellpadding="0" cellspacing="0" role="presentation"\s
                   width="100%" style="table-layout: fixed; vertical-align: top;\s
                   border-spacing: 0; border-collapse: collapse;
                   mso-table-lspace: 0pt; mso-table-rspace: 0pt;"\s
                   valign="top">
                   <tr style="vertical-align: top;" valign="top">
                   <td style="word-break: break-word; vertical-align: top;
                   padding-bottom: 0px; padding-left: 0px; padding-right: 0px;
                   padding-top: 0px; text-align: center; width: 100%;"\s
                   width="100%" align="center" valign="top">
                   <h1 style="color:#393d47;direction:ltr;
                   font-family:Tahoma, Verdana, Segoe, sans-serif;
                   font-size:25px;font-weight:normal;letter-spacing:normal;
                   line-height:120%;text-align:center;margin-top:0;margin-bottom:0;">
                   <strong>Esqueceu sua senha?</strong></h1>
                   </td>
                   </tr>
                   </table>
                   <!--[if mso]><table width="100%"\s
                   cellpadding="0"
                   cellspacing="0"\s
                   border="0"><tr>
                   <td style="padding-right: 10px; padding-left: 10px;
                   padding-top: 10px;
                   padding-bottom: 10px; font-family: Tahoma, Verdana, sans-serif"><![endif]-->
                   <div style="color:#393d47;font-family:Tahoma, Verdana, Segoe, sans-serif;
                   line-height:1.5;padding-top:10px;padding-right:10px;
                   padding-bottom:10px;padding-left:10px;">
                   <div class="txtTinyMce-wrapper"
                   style="line-height: 1.5;\s
                   font-size: 12px; font-family: Tahoma, Verdana, Segoe, sans-serif; color: #393d47;
                   mso-line-height-alt: 18px;">
                   <p style="margin: 0; font-size: 14px; line-height: 1.5;
                   word-break: break-word; text-align: center; mso-line-height-alt: 21px;
                   margin-top: 0; margin-bottom: 0;">
                   Não se preocupe, basta clicar no botão abaixo,\040
                   que enviamos uma nova senha para você,\s
                   lembre-se este link está disponível por 5 (cinco) horas</p>
                   </div>
                   </div>
                   <!--[if mso]></td></tr></table><![endif]-->
                   <div class="button-container" align="center"\s
                   style="padding-top:15px;padding-right:15px;padding-bottom:15px;
                   padding-left:15px;">
                   <!--[if mso]><table width="100%"
                   cellpadding="0" cellspacing="0"\s
                   border="0" style="border-spacing: 0; border-collapse: collapse;
                   mso-table-lspace:0pt; mso-table-rspace:0pt;">
                   <tr><td style="padding-top: 15px;\s
                   padding-right: 15px; padding-bottom: 15px;
                   padding-left: 15px" align="center">
                   <v:roundrect xmlns:v="urn:schemas-microsoft-com:vml"\s
                   xmlns:w="urn:schemas-microsoft-com:office:word"
                   href="[---URL_SENHA---]" style="height:43.5pt;
                   width:203.25pt;v-text-anchor:middle;" arcsize="35%"
                   strokeweight="0.75pt" strokecolor="#FFC727"\s
                   fillcolor="#ffc727"><w:anchorlock/>
                   <v:textbox inset="0,0,0,0"><center\s
                   style="color:#393d47; font-family:Tahoma, Verdana, sans-serif;
                   font-size:18px"><![endif]--><a href="[---URL_SENHA---]"\s
                   target="_blank" style="-webkit-text-size-adjust: none;
                   text-decoration: none; display: inline-block;
                   color: #393d47; background-color: #ffc727;\s
                   border-radius: 20px; -webkit-border-radius: 20px;\s
                   -moz-border-radius: 20px; width: auto; width: auto;
                   border-top: 1px solid #FFC727; border-right: 1px solid #FFC727;
                   border-bottom: 1px solid #FFC727;
                   border-left: 1px solid #FFC727; padding-top: 10px;
                   padding-bottom: 10px;
                   font-family: Tahoma, Verdana, Segoe, sans-serif;
                   text-align: center;
                   mso-border-alt: none; word-break: keep-all;">
                   <span style="padding-left:50px;padding-right:50px;
                   font-size:18px;display:inline-block;letter-spacing:normal;">
                   <span style="font-size: 16px; line-height: 2; word-break: break-word;
                    mso-line-height-alt: 32px;">
                   <span style="font-size: 18px; line-height: 36px;"\040
                   data-mce-style="font-size: 18px; line-height: 36px;">
                   <strong>SOLICITAR SENHA</strong></span></span></span></a>
                   <!--[if mso]></center></v:textbox></v:roundrect></td></tr></table><![endif]-->
                   </div>
                   <!--[if mso]><table width="100%" cellpadding="0"\s
                   cellspacing="0" border="0">
                   <tr><td style="padding-right: 10px; padding-left: 10px;
                   padding-top: 10px; padding-bottom: 5px;
                   font-family: Tahoma, Verdana, sans-serif"><![endif]-->
                   <div style="color:#393d47;
                   font-family:Tahoma, Verdana, Segoe, sans-serif;line-height:1.5;
                   padding-top:10px;padding-right:10px;padding-bottom:5px;
                   padding-left:10px;">
                   <div class="txtTinyMce-wrapper" style="line-height: 1.5;
                   font-size: 12px; font-family: Tahoma, Verdana, Segoe, sans-serif;
                   text-align: center; color: #393d47; mso-line-height-alt: 18px;">
                   <p style="margin: 0; line-height: 1.5; word-break: break-word;
                   font-size: 13px; mso-line-height-alt: 20px; mso-ansi-font-size: 14px;
                   margin-top: 0; margin-bottom: 0;"><span style="font-size: 13px;
                   mso-ansi-font-size: 14px;">
                   Caso, esta solicitação não foi sua, apenas desconsidere este e-mail.
                   </span></p>
                   </div>
                   </div>
                   <!--[if mso]></td></tr></table><![endif]-->
                   <!--[if (!mso)&(!IE)]><!-->
                   </div>
                   <!--<![endif]-->
                   </div>
                   </div>
                   <!--[if (mso)|(IE)]></td></tr></table><![endif]-->
                   <!--[if (mso)|(IE)]></td></tr></table></td></tr></table><![endif]-->
                   </div>
                   </div>
                   </div>
                   <div style="background-color:#f5f5f5;">
                   <div class="block-grid " style="min-width: 320px; max-width: 500px;
                   overflow-wrap: break-word; word-wrap: break-word; word-break: break-word;\s
                   Margin: 0 auto; background-color: transparent;">
                   <div style="border-collapse: collapse;display: table;width: 100%;
                   background-color:transparent;">
                   <!--[if (mso)|(IE)]><table
                   width="100%" cellpadding="0" cellspacing="0"\s
                   border="0" style="background-color:#f5f5f5;"><tr>
                   <td align="center"><table cellpadding="0"\s
                   cellspacing="0"
                   border="0"
                   style="width:500px">
                   <tr class="layout-full-width" style="background-color:transparent"><![endif]-->
                   <!--[if (mso)|(IE)]><td align="center"
                   width="500"\s
                   style="background-color:transparent;
                   width:500px; border-top: 0px solid transparent;
                   border-left: 0px solid transparent;\s
                   border-bottom: 0px solid transparent;
                   border-right: 0px solid transparent;"
                   valign="top"><table width="100%" cellpadding="0"
                   cellspacing="0" border="0">
                   <tr><td style="padding-right: 0px;
                   padding-left: 0px;
                   padding-top:0px;\s
                   padding-bottom:0px;"><![endif]-->
                   <div class="col num12" style="min-width: 320px;
                   max-width: 500px; display: table-cell; vertical-align: top; width: 500px;">
                   <div class="col_cont" style="width:100% !important;">
                   <!--[if (!mso)&(!IE)]><!-->
                   <div style="border-top:0px solid transparent;
                   border-left:0px solid transparent;
                   border-bottom:0px solid transparent; border-right:0px solid transparent;
                    padding-top:0px;
                   padding-bottom:0px; padding-right: 0px; padding-left: 0px;">
                   <!--<![endif]-->
                   <div class="img-container center autowidth big"\s
                   align="center" style="padding-right: 0px;
                   padding-left: 0px;">
                   <!--[if mso]><table width="100%" cellpadding="0"
                   cellspacing="0" border="0">
                   <tr style="line-height:0px"><td style="padding-right: 0px;padding-left: 0px;
                   " align="center"><![endif]--><img class="center autowidth"
                   align="center" border="0"
                   src="https://d1oco4z2z1fhwp.cloudfront.net/templates/default/2966/Btm.png"
                   style="text-decoration: none;
                   -ms-interpolation-mode: bicubic; height: auto;
                   border: 0; width: 500px; max-width: 100%; display: block;" width="500">
                   <!--[if mso]></td></tr></table><![endif]-->
                   </div>
                   <table class="divider" border="0" cellpadding="0"
                   cellspacing="0" width="100%" style="table-layout: fixed;
                   vertical-align: top; border-spacing: 0;
                   border-collapse: collapse; mso-table-lspace: 0pt;\s
                   mso-table-rspace: 0pt; min-width: 100%;
                   -ms-text-size-adjust: 100%; -webkit-text-size-adjust: 100%;"
                   role="presentation" valign="top">
                   <tbody>
                   <tr style="vertical-align: top;" valign="top">
                   <td class="divider_inner" style="word-break: break-word;
                   vertical-align: top; min-width: 100%; -ms-text-size-adjust: 100%;
                   -webkit-text-size-adjust: 100%; padding-top: 10px; padding-right: 10px;
                   padding-bottom: 10px; padding-left: 10px;" valign="top">
                   <table class="divider_content" border="0" cellpadding="0"\s
                   cellspacing="0" width="100%" style="table-layout: fixed; vertical-align: top;
                   border-spacing: 0; border-collapse: collapse; mso-table-lspace: 0pt;
                   mso-table-rspace: 0pt; border-top: 1px solid #BBBBBB; width: 100%;"
                   align="center" role="presentation" valign="top">
                   <tbody>
                   <tr style="vertical-align: top;" valign="top">
                   <td style="word-break: break-word; vertical-align: top;
                   -ms-text-size-adjust: 100%; -webkit-text-size-adjust: 100%;"
                   valign="top"><span></span></td>
                   </tr>
                   </tbody>
                   </table>
                   </td>
                   </tr>
                   </tbody>
                   </table>
                   <!--[if (!mso)&(!IE)]><!-->
                   </div>
                   <!--<![endif]-->
                   </div>
                   </div>
                   <!--[if (mso)|(IE)]></td></tr></table><![endif]-->
                   <!--[if (mso)|(IE)]></td></tr></table></td></tr></table><![endif]-->
                   </div>
                   </div>
                   </div>
                   <!--[if (mso)|(IE)]></td></tr></table><![endif]-->
                   </td>
                   </tr>
                   </tbody>
                   </table>
                   <!--[if (IE)]></div><![endif]-->
                   </body>
                   
                   </html>
                   """;
    }

    @Override
    public String getTemplate() {
        return TEMPLATE.replace("[---URL_SENHA---]", url);
    }

    @Override
    public String subject() {
        return "Xenon: Solicitação de renovação de senha.";
    }
}
