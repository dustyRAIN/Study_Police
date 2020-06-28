from rest_framework import serializers
from . import models

class PostMaterialInClassSerializer(serializers.ModelSerializer):

    class Meta:
        model = models.Materials
        fields = ['classroom', 'the_file', 'name']


class MaterialJustDetailsSerializer(serializers.ModelSerializer):

    class Meta:
        model = models.Materials
        fields = ['id', 'classroom', 'the_file', 'name', 'date_uploaded']
