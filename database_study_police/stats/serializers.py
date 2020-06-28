from rest_framework import serializers
from . import models

class AddTimeSerializer(serializers.ModelSerializer):

    class Meta:
        model = models.ReadingTime
        fields = ['classroom', 'student', 'material', 'duration']


class ModifyFrequencySerializer(serializers.ModelSerializer):

    class Meta:
        model = models.ContentFrequency
        fields = ['content_id', 'content_type', 'user', 'frequency', 'name']


class FrequecySerializer(serializers.ModelSerializer):
    class Meta:
        model = models.ContentFrequency
        fields = ['id', 'content_id', 'content_type', 'user', 'frequency', 'name']