from django.conf.urls import url
from django.contrib import admin
from django.urls import path

from . import views

urlpatterns = [
    path('upload/', views.MaterialsView.as_view(), name = 'uploadMaterial'),
    path('<classroom>', views.MaterialsJustDetailsView.as_view(), name = 'classMaterials'),
    path('get/', views.SingleMaterialView.as_view(), name = 'getMaterial'),
]

