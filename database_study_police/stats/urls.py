from django.conf.urls import url
from django.contrib import admin
from django.urls import path

from . import views

urlpatterns = [
    path('readtime/student/', views.StudentReadingTimeView.as_view(), name = 'studentreadtime'),
    path('readtime/material/', views.MaterialReadingTimeView.as_view(), name = 'materialreadtime'),
    path('readtime/add/', views.AddReadingTimeView.as_view(), name = 'addreadtime'),
    path('frequency/update/', views.ContentFrequencyView.as_view(), name = 'updatefrequency'),
    path('frequency/get/', views.GetUserFrequentContentView.as_view(), name = 'userfrequency'),
]