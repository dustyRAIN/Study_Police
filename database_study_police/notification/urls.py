from django.conf.urls import url
from django.contrib import admin
from django.urls import path

from . import views

urlpatterns = [
    path('all/', views.GetNotificationView.as_view(), name = 'allnotification'),
    path('seen/', views.NotificationSeenView.as_view(), name = 'seennotification'),
]