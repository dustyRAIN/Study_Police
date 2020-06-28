from django.shortcuts import render
from django.http import HttpResponse
from users.models import CustomUser
from classes.models import Classroom, ClassHasStudent
from materials.models import Materials
def Class_materials(request, id, *args, **kwargs):
    user = CustomUser.objects.get(id=2)
    context = {
        'User': user
    }
    return render(request, "Class_materials.html", context)

def Homepage(request, *args, **kwargs):
    created_class = Classroom.objects.filter(creator=2)
    joined_class = ClassHasStudent.objects.filter(student=2) 
    user = CustomUser.objects.get(id=2)
    context = {
        'User': user,
        'created_class': created_class, 
        'joined_class': joined_class,
    }
    return render(request, "Homepage.html", context)

def Web_Class(request, id, *args, **kwargs):
    print(id)
    user = CustomUser.objects.get(id=2)
    class_materials = Materials.objects.filter(classroom=id)
    students = ClassHasStudent.objects.filter(classroom=id)

    context = {
        'User': user,
        'class_materials': class_materials,
        'students': students,
    }
    return render(request, "Web_Class.html", context)

def SignIn(request, *args, **kwargs):

    context = {

    }
    return render(request, "SignIn.html", context)
