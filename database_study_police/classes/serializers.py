from rest_framework import serializers
from . import models
from .utils import randomStringDigits

from users.models import CustomUser

class CreateClassSerializer(serializers.ModelSerializer):
    class Meta:
        model = models.Classroom
        fields = ['name', 'date_created', 'creator']


class ClassroomDetailsSerializer(serializers.ModelSerializer):
    class Meta:
        model = models.Classroom
        fields = ['id', 'name', 'access_code', 'date_created', 'creator']


class ClassroomStudentPostSerializer(serializers.ModelSerializer):
    class Meta:
        model = models.ClassHasStudent
        fields = ['classroom', 'student']


class ClassUserNameSerializer(serializers.ModelSerializer):
    class Meta:
        model = models.CustomUser
        fields = ['id','name']


class ClassDetailsInDetailsSerializer(serializers.ModelSerializer):
    
    creator = ClassUserNameSerializer(read_only = True)

    class Meta:
        model = models.Classroom
        fields = ['id', 'name', 'access_code', 'date_created', 'creator']



class JoinedClassSerializer(serializers.ModelSerializer):

    classroom = ClassDetailsInDetailsSerializer(read_only = True)

    class Meta:
        model = models.ClassHasStudent
        fields = ['classroom', 'student', 'date_joined']


class ClassStudentDetailsSerializer(serializers.ModelSerializer):

    student = ClassUserNameSerializer(read_only = True)

    class Meta:
        model = models.ClassHasStudent
        fields = ['classroom', 'student']



