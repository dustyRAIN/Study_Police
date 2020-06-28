from rest_framework import serializers
from . import models

from rest_auth.registration.serializers import RegisterSerializer

class CustomRegisterSerializer(RegisterSerializer):
    

    email = serializers.EmailField(required=True)
    password1 = serializers.CharField(write_only=True)
    name = serializers.CharField(required=True)
    gender = serializers.IntegerField(required=True)
    image = serializers.ImageField(required=True)
    

    def get_cleaned_data(self):
        super(CustomRegisterSerializer, self).get_cleaned_data()
        
        return {
            'username': self.validated_data.get('username', ''),
            'password1': self.validated_data.get('password1', ''),
            'email': self.validated_data.get('email', ''),
            'name': self.validated_data.get('name', ''),
            'gender': self.validated_data.get('gender', ''),
            'image': self.validated_data.get('image', ''),
        }



class RegisterWOPassSerializer(RegisterSerializer):
    

    email = serializers.EmailField(required=True)
    password1 = serializers.CharField(write_only=True)
    name = serializers.CharField(required=True)
    gender = serializers.IntegerField(required=True)
    image = serializers.ImageField(required=True)
    

    def get_cleaned_data(self):
        super(RegisterWOPassSerializer, self).get_cleaned_data()
        
        return {
            'username': self.validated_data.get('username', ''),
            'password1': self.validated_data.get('password1', ''),
            'email': self.validated_data.get('email', ''),
            'name': self.validated_data.get('name', ''),
            'gender': self.validated_data.get('gender', ''),
            'image': self.validated_data.get('image', ''),
        }

    



class CustomUserDetailsSerializer(serializers.ModelSerializer):

    class Meta:
        model = models.CustomUser
        fields = ('id','email','name','gender', 'image')
        read_only_fields = ('email',)


class DemoPhotoSerializer(serializers.ModelSerializer):

    class Meta:
        model = models.DemoPhoto
        fields = ['name', 'size', 'image']


class EmailTakenSerializer(serializers.ModelSerializer):

    class Meta:
        model = models.EmailByProvider
        fields = ['email', 'provider']