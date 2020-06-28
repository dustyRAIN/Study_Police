from django.contrib import admin
from django.urls import path
from django.conf.urls import url

from . import views

urlpatterns = [
    path('exists/<email>', views.AccountExistsAPICall.as_view(), name = 'exists'),
    path('registration/', views.CustomRegisterView.as_view(), name = "customRegister"),
    path('google/callback/', views.GoogleCallback.as_view(), name = "googleRedirect"),
    path('google/login/', views.GoogleLoginFromToken.as_view(), name = "googleLogin"),
    path('provider/taken/', views.ProviderTakenEmail.as_view(), name = "emailTakenProvider"),
]