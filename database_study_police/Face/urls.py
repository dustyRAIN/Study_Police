from django.contrib import admin
from django.urls import path
from django.conf.urls import url

from . import views

urlpatterns = [
    path('post/', views.RegisterFaceView.as_view(), name = 'postface'),
    path('match/', views.MatchFaceView.as_view(), name = 'matchface'),
    path('exists/', views.HasFaceView.as_view(), name = 'faceexists'),
    path('delete/', views.DeleteFaceView.as_view(), name = 'facedelete'),
]