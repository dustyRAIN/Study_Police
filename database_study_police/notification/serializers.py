from rest_framework import serializers
from . import models
from classes.models import Classroom

class PostNotificationSerializer(serializers.ModelSerializer):

    class Meta:
        model = models.GeneralNotification
        fields = ['classroom', 'user', 'seen', 'noti_type', 'time']



class ClassNameSerializer(serializers.ModelSerializer):

    class Meta:
        model = Classroom
        fields = ['id', 'name']


class GetNotificationSerializer(serializers.ModelSerializer):

    classroom = ClassNameSerializer(read_only = True)

    class Meta:
        model = models.GeneralNotification
        fields = ['id', 'classroom', 'user', 'seen', 'noti_type', 'time']