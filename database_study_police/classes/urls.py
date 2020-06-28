from django.contrib import admin
from django.urls import path
from django.conf.urls import url

from . import views

urlpatterns = [
    path('<id>', views.GetAClassDataView.as_view(), name = 'classroomdetails'),
    path('create/', views.CreateClassroom.as_view(), name = 'createclassroom'),
    path('created/<creator>', views.ClassroomDetailsView.as_view(), name = 'createdclasses'),
    path('join/<student>/<access_code>', views.AddStudentView.as_view(), name = 'joinclassroom'),
    path('joined/<student>', views.JoinedClassesView.as_view(), name = 'joinedclasses'),
    path('students/<classroom>', views.ClassStudentsView.as_view(), name = 'joinedclasses'),
    
]