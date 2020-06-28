from rest_framework import serializers
from . import models

class RegisterFaceSerializer(serializers.ModelSerializer):

    class Meta:
        model = models.TestFace
        fields = ['test_image', 'user']